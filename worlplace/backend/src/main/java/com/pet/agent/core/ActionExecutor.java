package com.pet.agent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pet.agent.agents.MallAgent;
import com.pet.agent.entity.Reminder;
import com.pet.agent.service.ReminderService;
import com.pet.common.exception.BusinessException;
import com.pet.dto.AppointmentRequest;
import com.pet.entity.HealthRecord;
import com.pet.entity.Pet;
import com.pet.dto.OrderRequest;
import com.pet.dto.ProductQueryRequest;
import com.pet.service.AppointmentService;
import com.pet.service.CartService;
import com.pet.service.HealthRecordService;
import com.pet.service.OrderService;
import com.pet.service.PetService;
import com.pet.service.ProductService;
import com.pet.service.ServiceService;
import com.pet.vo.CartItemVO;
import com.pet.vo.OrderVO;
import com.pet.vo.AppointmentVO;
import com.pet.vo.ProductVO;
import com.pet.vo.ServiceVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ActionExecutor {

    private static final Logger log = LoggerFactory.getLogger(ActionExecutor.class);
    private static final String ACTION_JSON_PREFIX = "{\"action\":";

    private final ObjectMapper objectMapper;
    private final PetService petService;
    private final HealthRecordService healthRecordService;
    private final AppointmentService appointmentService;
    private final ReminderService reminderService;
    private final ServiceService serviceService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CartService cartService;
    private final MallAgent mallAgent;

    public ActionExecutor(ObjectMapper objectMapper, PetService petService,
                          HealthRecordService healthRecordService,
                          AppointmentService appointmentService,
                          ReminderService reminderService,
                          ServiceService serviceService,
                          OrderService orderService,
                          ProductService productService,
                          CartService cartService,
                          MallAgent mallAgent) {
        this.objectMapper = objectMapper;
        this.petService = petService;
        this.healthRecordService = healthRecordService;
        this.appointmentService = appointmentService;
        this.reminderService = reminderService;
        this.serviceService = serviceService;
        this.orderService = orderService;
        this.productService = productService;
        this.cartService = cartService;
        this.mallAgent = mallAgent;
    }

    static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static class ActionResult {
        private final String cleanReply;
        private final String confirmation;
        private final boolean actionExecuted;
        private final String navigate;
        private final String navigateParams;

        public ActionResult(String cleanReply, String confirmation, boolean actionExecuted) {
            this(cleanReply, confirmation, actionExecuted, null, null);
        }

        public ActionResult(String cleanReply, String confirmation, boolean actionExecuted,
                           String navigate, String navigateParams) {
            this.cleanReply = cleanReply;
            this.confirmation = confirmation;
            this.actionExecuted = actionExecuted;
            this.navigate = navigate;
            this.navigateParams = navigateParams;
        }

        public String getCleanReply() { return cleanReply; }
        public String getConfirmation() { return confirmation; }
        public boolean isActionExecuted() { return actionExecuted; }
        public String getNavigate() { return navigate; }
        public String getNavigateParams() { return navigateParams; }
    }

    private static class ExecResult {
        final String message;
        final String navigate;
        final String navigateParams;
        ExecResult(String message) { this(message, null, null); }
        ExecResult(String message, String navigate, String navigateParams) {
            this.message = message; this.navigate = navigate; this.navigateParams = navigateParams;
        }
    }

    public ActionResult process(String llmReply, Long userId) {
        JsonNode actionJson = extractActionJson(llmReply);
        if (actionJson == null) {
            return new ActionResult(llmReply, null, false);
        }

        String cleanReply = removeActionJson(llmReply);
        try {
            String operation = actionJson.get("operation").asText();
            String entity = actionJson.get("entity").asText();
            JsonNode fields = actionJson.get("fields");

            log.info("ActionExecutor.process: operation={}, entity={}, fields={}", operation, entity, fields);

            ExecResult result = executeAction(operation, entity, fields, userId);
            return new ActionResult(cleanReply, result.message, true,
                    result.navigate, result.navigateParams);
        } catch (BusinessException e) {
            log.warn("Action execution business error: {}", e.getMessage());
            return new ActionResult(cleanReply, e.getMessage(), true);
        } catch (Exception e) {
            log.warn("Action execution failed", e);
            return new ActionResult(cleanReply, "操作未能完成，请稍后重试或联系管理员。", true);
        }
    }

    private JsonNode extractActionJson(String llmReply) {
        if (llmReply == null) return null;
        String text = llmReply.trim();
        int start = text.lastIndexOf(ACTION_JSON_PREFIX);
        if (start < 0) return null;
        String json = extractJsonByBraces(text, start);
        if (json == null) return null;
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode action = root.get("action");
            if (action != null && action.has("operation") && action.has("entity")) {
                return action;
            }
        } catch (Exception e) {
            log.debug("No valid action JSON found");
        }
        return null;
    }

    private String extractJsonByBraces(String text, int start) {
        int braceCount = 0;
        boolean inString = false;
        boolean escaped = false;
        for (int i = start; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (escaped) { escaped = false; continue; }
            if (ch == '\\') { escaped = true; continue; }
            if (ch == '"') { inString = !inString; continue; }
            if (inString) continue;
            if (ch == '{') { braceCount++; }
            else if (ch == '}') {
                braceCount--;
                if (braceCount == 0) {
                    return text.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private String removeActionJson(String llmReply) {
        if (llmReply == null) return "";
        String text = llmReply.trim();
        int start = text.lastIndexOf(ACTION_JSON_PREFIX);
        if (start < 0) return text;
        String json = extractJsonByBraces(text, start);
        if (json == null) return text;
        String before = text.substring(0, start).trim();
        String after = text.substring(start + json.length()).trim();
        return (before + " " + after).trim();
    }

    private ExecResult executeAction(String operation, String entity, JsonNode fields, Long userId) {
        switch (entity) {
            case "APPOINTMENT":
                return new ExecResult(executeAppointment(operation, fields, userId));
            case "HEALTH_RECORD":
                return new ExecResult(executeHealthRecord(operation, fields, userId));
            case "PET":
                return new ExecResult(executePet(operation, fields, userId));
            case "REMINDER":
                return new ExecResult(executeReminder(operation, fields, userId));
            case "ORDER":
                return new ExecResult(executeOrder(operation, fields, userId));
            case "PRODUCT_SEARCH":
                return executeProductSearch(fields, userId);
            case "CART_ADD":
                return new ExecResult(executeCartAdd(fields, userId));
            case "PAGE_NAVIGATE":
                return executePageNavigate(fields);
            default:
                return new ExecResult("暂不支持该类型操作。");
        }
    }

    private String executeAppointment(String operation, JsonNode fields, Long userId) {
        if (!"ADD".equals(operation)) {
            return "预约仅支持新增操作，取消请前往预约页面。";
        }

        Long petId = resolvePetId(fields, userId);
        if (petId == null) return "未找到对应宠物，请先确认宠物信息。";

        Long serviceId = resolveServiceId(fields);
        if (serviceId == null) return "未找到对应服务，请确认服务名称。";

        String datetimeStr = fields.has("appointmentDatetime")
                ? fields.get("appointmentDatetime").asText() : null;
        if (datetimeStr == null) return "请提供预约时间。";

        LocalDateTime appointmentDatetime;
        try {
            appointmentDatetime = LocalDateTime.parse(datetimeStr, DATETIME_FMT);
        } catch (Exception e) {
            return "预约时间格式有误，请使用 yyyy-MM-dd HH:mm 格式。";
        }

        String notes = fields.has("notes") ? fields.get("notes").asText() : null;

        AppointmentRequest req = new AppointmentRequest();
        req.setPetId(petId);
        req.setServiceId(serviceId);
        req.setAppointmentDatetime(appointmentDatetime);
        req.setNotes(notes);

        AppointmentVO result = appointmentService.createAppointment(req, userId);
        return "已为您创建预约（单号：" + result.getOrderNo()
                + "），时间：" + appointmentDatetime.format(DATETIME_FMT) + "。";
    }

    private String executeHealthRecord(String operation, JsonNode fields, Long userId) {
        if (!"ADD".equals(operation)) {
            return "健康记录仅支持新增操作。";
        }

        Long petId = resolvePetId(fields, userId);
        if (petId == null) return "未找到对应宠物，请先确认宠物信息。";

        String recordType = fields.has("recordType") ? fields.get("recordType").asText() : null;
        if (recordType == null) return "请提供记录类型（如体检、疫苗、驱虫等）。";

        HealthRecord record = new HealthRecord();
        record.setPetId(petId);
        record.setRecordType(recordType);

        if (fields.has("recordDate")) {
            try {
                record.setRecordDate(LocalDate.parse(fields.get("recordDate").asText()));
            } catch (Exception e) { log.debug("Skipping recordDate parse"); }
        }
        if (fields.has("diagnosis")) record.setDiagnosis(fields.get("diagnosis").asText());
        if (fields.has("medicine")) record.setMedicine(fields.get("medicine").asText());
        if (fields.has("notes")) record.setNotes(fields.get("notes").asText());
        if (fields.has("weight")) {
            try { record.setWeight(new BigDecimal(fields.get("weight").asText())); }
            catch (Exception e) { log.debug("Skipping weight parse"); }
        }

        healthRecordService.save(record);
        return "已为宠物添加" + recordType + "记录。";
    }

    private String executePet(String operation, JsonNode fields, Long userId) {
        if ("ADD".equals(operation)) {
            String name = fields.has("name") ? fields.get("name").asText() : null;
            String type = fields.has("type") ? fields.get("type").asText() : null;
            if (name == null || type == null) return "请提供宠物名称和类型。";

            Pet pet = new Pet();
            pet.setUserId(userId);
            pet.setName(name);
            pet.setType(type.toUpperCase());
            if (fields.has("breed")) pet.setBreed(fields.get("breed").asText());
            if (fields.has("gender")) pet.setGender(fields.get("gender").asInt());
            if (fields.has("birthDate")) {
                try { pet.setBirthDate(LocalDate.parse(fields.get("birthDate").asText())); }
                catch (Exception e) { log.debug("Skipping birthDate parse"); }
            }
            if (fields.has("color")) pet.setColor(fields.get("color").asText());
            if (fields.has("weight")) {
                try { pet.setWeight(fields.get("weight").asDouble()); }
                catch (Exception e) { log.debug("Skipping weight parse"); }
            }
            petService.save(pet);
            return "已添加宠物「" + name + "」。";
        }

        if ("MODIFY".equals(operation)) {
            Pet pet = resolvePet(fields, userId);
            if (pet == null) return "未找到对应宠物，请先确认宠物名称。";

            if (fields.has("name")) pet.setName(fields.get("name").asText());
            if (fields.has("breed")) pet.setBreed(fields.get("breed").asText());
            if (fields.has("color")) pet.setColor(fields.get("color").asText());
            if (fields.has("weight")) {
                try { pet.setWeight(fields.get("weight").asDouble()); }
                catch (Exception e) { log.debug("Skipping weight parse"); }
            }
            if (fields.has("birthDate")) {
                try { pet.setBirthDate(LocalDate.parse(fields.get("birthDate").asText())); }
                catch (Exception e) { log.debug("Skipping birthDate parse"); }
            }
            petService.updateById(pet);
            return "已更新宠物「" + pet.getName() + "」的信息。";
        }

        return "宠物档案仅支持新增和修改操作。";
    }

    private String executeReminder(String operation, JsonNode fields, Long userId) {
        if (!"ADD".equals(operation) && !"DELETE".equals(operation)) {
            return "提醒仅支持新增和删除操作。";
        }

        if ("DELETE".equals(operation)) {
            return "删除提醒请前往「提醒管理」页面操作。";
        }

        Long petId = resolvePetId(fields, userId);
        if (petId == null) return "未找到对应宠物，请先确认宠物信息。";

        String title = fields.has("title") ? fields.get("title").asText() : null;
        if (title == null) return "请提供提醒标题。";

        String dateStr = fields.has("remindDate") ? fields.get("remindDate").asText() : null;
        if (dateStr == null) return "请提供提醒日期。";

        Reminder reminder = new Reminder();
        reminder.setUserId(userId);
        reminder.setPetId(petId);
        reminder.setTitle(title);
        reminder.setReminderType(fields.has("reminderType")
                ? fields.get("reminderType").asText() : "CUSTOM");
        if (fields.has("description")) reminder.setDescription(fields.get("description").asText());
        try { reminder.setRemindDate(LocalDate.parse(dateStr)); }
        catch (Exception e) { return "日期格式有误，请使用 yyyy-MM-dd 格式。"; }

        reminderService.createReminder(reminder);
        return "已为宠物创建提醒：「" + title + "」，日期：" + dateStr + "。";
    }

    private String executeOrder(String operation, JsonNode fields, Long userId) {
        if (!"ADD".equals(operation)) {
            return "订单仅支持下单操作，取消/修改请前往订单页面。";
        }

        log.info("executeOrder fields: hasItems={}, isArray={}, fieldsJson={}",
                fields != null && fields.has("items"),
                fields != null && fields.has("items") && fields.get("items").isArray(),
                fields);

        if (fields == null || !fields.has("items") || !fields.get("items").isArray()) {
            return "请提供订单商品列表。";
        }

        String shippingName = fields.has("shippingName") ? fields.get("shippingName").asText() : null;
        String shippingPhone = fields.has("shippingPhone") ? fields.get("shippingPhone").asText() : null;
        String shippingAddress = fields.has("shippingAddress") ? fields.get("shippingAddress").asText() : null;

        if (shippingName == null || shippingPhone == null || shippingAddress == null) {
            return "请提供收件人、联系电话和收货地址。";
        }

        OrderRequest request = new OrderRequest();
        request.setShippingName(shippingName);
        request.setShippingPhone(shippingPhone);
        request.setShippingAddress(shippingAddress);

        if (fields.has("paymentMethod")) {
            request.setPaymentMethod(fields.get("paymentMethod").asText());
        }
        if (fields.has("notes")) {
            request.setNotes(fields.get("notes").asText());
        }

        List<OrderRequest.OrderItemRequest> items = new java.util.ArrayList<>();
        for (JsonNode item : fields.get("items")) {
            JsonNode productIdNode = item.get("productId");
            if (productIdNode == null || productIdNode.isNull()) {
                return "订单商品列表中缺少 productId 字段，请确认商品信息。";
            }
            OrderRequest.OrderItemRequest orderItem = new OrderRequest.OrderItemRequest();
            orderItem.setProductId(productIdNode.asLong());
            orderItem.setQuantity(item.has("quantity") ? item.get("quantity").asInt() : 1);
            items.add(orderItem);
        }
        request.setItems(items);

        List<Long> invalidIds = new java.util.ArrayList<>();
        List<String> resolvedNames = new java.util.ArrayList<>();
        for (OrderRequest.OrderItemRequest item : items) {
            try {
                ProductVO product = productService.getProductById(item.getProductId());
                resolvedNames.add(product.getName() + " x" + item.getQuantity());
            } catch (Exception e) {
                invalidIds.add(item.getProductId());
            }
        }
        if (!invalidIds.isEmpty()) {
            StringBuilder errMsg = new StringBuilder();
            errMsg.append("以下商品ID不存在于系统中：");
            for (Long id : invalidIds) errMsg.append(id).append(" ");
            errMsg.append("\n\n当前可用的商品ID：\n");
            ProductQueryRequest validQuery = new ProductQueryRequest();
            validQuery.setStatus(1); validQuery.setPageNum(1); validQuery.setPageSize(50);
            validQuery.setSortBy("sales"); validQuery.setSortOrder("DESC");
            try {
                for (ProductVO vp : productService.getProductList(validQuery).getRecords()) {
                    errMsg.append("[ID:").append(vp.getId()).append("] ")
                          .append(vp.getName()).append("  ¥").append(vp.getPrice()).append("\n");
                }
            } catch (Exception ex) {
                errMsg.append("（暂时无法加载商品列表，请稍后重试）");
            }
            return errMsg.toString();
        }

        OrderVO result = orderService.createOrder(request, userId);
        return "已为您创建订单（单号：" + result.getOrderNo() + "），共" + items.size()
                + "件商品（" + String.join("、", resolvedNames) + "），收货人：" + shippingName + "。";
    }

    private Long resolvePetId(JsonNode fields, Long userId) {
        if (fields.has("petId")) return fields.get("petId").asLong();

        String petName = fields.has("petName") ? fields.get("petName").asText() : null;
        if (petName == null) return null;

        Pet pet = findPetByName(userId, petName);
        return pet != null ? pet.getId() : null;
    }

    private Pet resolvePet(JsonNode fields, Long userId) {
        if (fields.has("petId")) {
            return petService.getById(fields.get("petId").asLong());
        }
        String petName = fields.has("petName") ? fields.get("petName").asText() : null;
        if (petName == null) return null;
        return findPetByName(userId, petName);
    }

    private Pet findPetByName(Long userId, String name) {
        List<Pet> pets = petService.lambdaQuery()
                .eq(Pet::getUserId, userId)
                .like(Pet::getName, name)
                .list();
        if (pets.size() == 1) return pets.get(0);
        if (pets.size() > 1) {
            return pets.stream()
                    .filter(p -> p.getName().equals(name))
                    .findFirst()
                    .orElse(pets.get(0));
        }
        return null;
    }

    private Long resolveServiceId(JsonNode fields) {
        if (fields.has("serviceId")) return fields.get("serviceId").asLong();

        String serviceName = fields.has("serviceName") ? fields.get("serviceName").asText() : null;
        String serviceType = fields.has("serviceType") ? fields.get("serviceType").asText() : null;
        String lookup = serviceName != null ? serviceName : serviceType;
        if (lookup == null) return null;

        List<ServiceVO> services = serviceService.getAllActiveServices();
        for (ServiceVO sv : services) {
            if (sv.getName() != null && sv.getName().contains(lookup)) return sv.getId();
            if (sv.getCategory() != null && sv.getCategory().contains(lookup)) return sv.getId();
        }
        if (!services.isEmpty()) return services.get(0).getId();
        return null;
    }

    private ExecResult executeProductSearch(JsonNode fields, Long userId) {
        String keyword = fields.has("keyword") ? fields.get("keyword").asText() : "";
        String category = fields.has("category") ? fields.get("category").asText() : "";

        ProductQueryRequest query = new ProductQueryRequest();
        query.setKeyword(keyword);
        query.setCategory(category);
        query.setStatus(1);
        query.setPageNum(1);
        query.setPageSize(20);
        if (fields.has("sortBy")) query.setSortBy(fields.get("sortBy").asText());
        else query.setSortBy("sales");
        query.setSortOrder("DESC");

        List<ProductVO> results = productService.getProductList(query).getRecords();
        mallAgent.cacheSearchResults(userId, results, keyword);

        if (results.isEmpty()) {
            return new ExecResult("没有找到匹配\"" + keyword + "\"的商品，铲屎官换个关键词试试？",
                    "/dashboard/shop", "keyword=" + keyword);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("为您找到").append(results.size()).append("件商品：\n");
        for (ProductVO p : results) {
            sb.append("[ID:").append(p.getId()).append("] ")
              .append(p.getName()).append("  ¥").append(p.getPrice())
              .append("  库存:").append(p.getStock() != null ? p.getStock() : 0);
            if (p.getCategoryName() != null) sb.append("  ").append(p.getCategoryName());
            sb.append("\n");
        }
        return new ExecResult(sb.toString(),
                "/dashboard/shop", "keyword=" + keyword + (category.isEmpty() ? "" : "&category=" + category));
    }

    private String executeCartAdd(JsonNode fields, Long userId) {
        if (!fields.has("productId")) return "请提供要添加的商品ID。";
        Long productId = fields.get("productId").asLong();
        int quantity = fields.has("quantity") ? fields.get("quantity").asInt() : 1;

        try {
            CartItemVO item = cartService.addToCart(userId, productId, quantity);
            return "已将" + item.getProductName() + " x" + quantity + "加入购物车。";
        } catch (Exception e) {
            log.warn("Cart add failed: {}", e.getMessage());
            return "加入购物车失败，请检查商品是否存在或有库存。";
        }
    }

    private ExecResult executePageNavigate(JsonNode fields) {
        if (!fields.has("page")) return new ExecResult("请指定要打开的页面。");

        String page = fields.get("page").asText();
        String params = fields.has("params") ? fields.get("params").asText() : "";

        switch (page) {
            case "shop": return new ExecResult("正在为您打开商城页面。", "/dashboard/shop", params);
            case "cart": return new ExecResult("正在为您打开购物车。", "/dashboard/cart", "");
            case "appointment": return new ExecResult("正在为您打开预约页面。", "/dashboard/services", "");
            case "pets": return new ExecResult("正在为您打开宠物管理页面。", "/dashboard/pets", "");
            case "orders": return new ExecResult("正在为您打开订单列表。", "/dashboard/orders", "");
            case "address": return new ExecResult("正在为您打开地址管理。", "/user/address", "");
            case "health": return new ExecResult("正在为您打开健康管理。", "/dashboard/health", "");
            default: return new ExecResult("正在为您跳转到" + page + "页面。", "/dashboard/" + page, params);
        }
    }
}
