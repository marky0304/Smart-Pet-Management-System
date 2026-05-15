package com.pet.agent.agents;

import com.pet.agent.core.Agent;
import com.pet.agent.core.AgentContext;
import com.pet.agent.core.DomainType;
import com.pet.agent.core.OperationType;
import com.pet.agent.prompt.PromptTemplates;
import com.pet.dto.OrderQueryRequest;
import com.pet.dto.ProductQueryRequest;
import com.pet.entity.Pet;
import com.pet.service.AddressService;
import com.pet.service.OrderService;
import com.pet.service.ProductService;
import com.pet.vo.AddressVO;
import com.pet.vo.OrderVO;
import com.pet.vo.ProductVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MallAgent implements Agent {

    private final ProductService productService;
    private final OrderService orderService;
    private final AddressService addressService;

    private final ConcurrentHashMap<Long, SearchCacheEntry> searchCache = new ConcurrentHashMap<>();

    private static class SearchCacheEntry {
        final List<ProductVO> products;
        final String keyword;
        SearchCacheEntry(List<ProductVO> products, String keyword) {
            this.products = products;
            this.keyword = keyword;
        }
    }

    public void cacheSearchResults(Long userId, List<ProductVO> products, String keyword) {
        if (products != null && !products.isEmpty()) {
            searchCache.put(userId, new SearchCacheEntry(products, keyword));
        }
    }

    public MallAgent(ProductService productService, OrderService orderService,
                     AddressService addressService) {
        this.productService = productService;
        this.orderService = orderService;
        this.addressService = addressService;
    }

    @Override
    public DomainType getDomain() {
        return DomainType.MALL;
    }

    @Override
    public String getName() {
        return "商城服务Agent";
    }

    @Override
    public String buildSystemPrompt(AgentContext ctx, OperationType operation) {
        String base = PromptTemplates.buildPrompt(PromptTemplates.MALL_ROLE, operation.name());
        if (ctx.hasPets()) {
            StringBuilder petHint = new StringBuilder("\n\n用户宠物信息，用于推荐商品：\n");
            for (Pet pet : ctx.getPets()) {
                petHint.append("- ").append(pet.getName()).append("：")
                       .append(pet.getBreed() != null ? pet.getBreed() : "")
                       .append("，").append(pet.getAge() != null ? pet.getAge() + "岁" : "")
                       .append("\n");
            }
            base += petHint.toString();
        }
        return base;
    }

    @Override
    public String buildDataContext(AgentContext ctx) {
        StringBuilder sb = new StringBuilder();

        sb.append("【全部在售商品 — 下单时只能使用下面的 [ID:X] 中的数字！】\n");
        ProductQueryRequest allQuery = new ProductQueryRequest();
        allQuery.setStatus(1);
        allQuery.setPageNum(1);
        allQuery.setPageSize(50);
        allQuery.setSortBy("sales");
        allQuery.setSortOrder("DESC");
        try {
            List<ProductVO> allProducts = productService.getProductList(allQuery).getRecords();
            if (allProducts.isEmpty()) {
                sb.append("  暂无商品数据\n");
            } else {
                for (ProductVO p : allProducts) {
                    sb.append("[ID:").append(p.getId()).append("] ")
                      .append(p.getName())
                      .append("  ¥").append(p.getPrice())
                      .append("  分类:").append(p.getCategoryName() != null ? p.getCategoryName() : p.getCategory());
                    if (p.getStock() != null) sb.append("  库存:").append(p.getStock());
                    if (p.getSales() != null && p.getSales() > 0) sb.append("  已售").append(p.getSales());
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("  商品数据加载失败\n");
        }

        SearchCacheEntry cached = searchCache.get(ctx.getUserId());
        if (cached != null && cached.products != null && !cached.products.isEmpty()) {
            sb.append("\n【上次搜索\"").append(cached.keyword != null ? cached.keyword : "")
              .append("\"的结果 — 用户可能想从这些商品中下单】\n");
            for (ProductVO p : cached.products) {
                sb.append("[ID:").append(p.getId()).append("] ")
                  .append(p.getName()).append("  ¥").append(p.getPrice());
                if (p.getCategoryName() != null) sb.append("  ").append(p.getCategoryName());
                sb.append("\n");
            }
        }

        sb.append("\n【热销推荐】\n");
        List<ProductVO> hotProducts = productService.getHotProducts(5);
        if (!hotProducts.isEmpty()) {
            for (ProductVO p : hotProducts) {
                sb.append("[ID:").append(p.getId()).append("] ")
                  .append(p.getName()).append("  ¥").append(p.getPrice())
                  .append("  已售").append(p.getSales() != null ? p.getSales() : 0).append("件\n");
            }
        }

        sb.append("\n商品分类：食品(FOOD)、玩具(TOY)、用品(SUPPLY)、清洁(CLEAN)、药品(MEDICINE)\n");

        sb.append("\n用户收货地址（isDefault=1 为默认地址，下单时优先使用）：\n");
        try {
            List<AddressVO> addresses = addressService.getAddressList(ctx.getUserId());
            if (addresses.isEmpty()) {
                sb.append("  暂无地址\n");
            } else {
                for (AddressVO a : addresses) {
                    String fullAddress = (a.getProvince() != null ? a.getProvince() : "")
                            + (a.getCity() != null ? a.getCity() : "")
                            + (a.getDistrict() != null ? a.getDistrict() : "")
                            + (a.getDetailAddress() != null ? a.getDetailAddress() : "");
                    sb.append("- ").append(a.getReceiverName())
                      .append(" ").append(a.getReceiverPhone())
                      .append(" ").append(fullAddress);
                    if (a.getIsDefault() != null && a.getIsDefault() == 1) {
                        sb.append("【默认地址】");
                    }
                    sb.append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("  暂无地址\n");
        }

        sb.append("\n用户近期订单：\n");
        OrderQueryRequest orderQuery = new OrderQueryRequest();
        orderQuery.setPageNum(1);
        orderQuery.setPageSize(5);
        try {
            List<OrderVO> recentOrders = orderService.getMyOrders(orderQuery, ctx.getUserId()).getRecords();
            if (recentOrders.isEmpty()) {
                sb.append("  暂无订单\n");
            } else {
                for (OrderVO o : recentOrders) {
                    sb.append("- 单号：").append(o.getOrderNo())
                      .append("，状态：").append(o.getStatus())
                      .append("，金额：¥").append(o.getTotalAmount())
                      .append("\n");
                }
            }
        } catch (Exception e) {
            sb.append("  暂无订单\n");
        }

        return sb.toString();
    }

    public List<ProductVO> searchProducts(String keyword, String category) {
        com.pet.dto.ProductQueryRequest query = new com.pet.dto.ProductQueryRequest();
        query.setKeyword(keyword != null ? keyword : "");
        query.setCategory(category != null ? category : "");
        query.setStatus(1);
        query.setPageNum(1);
        query.setPageSize(20);
        query.setSortBy("sales");
        query.setSortOrder("DESC");
        return productService.getProductList(query).getRecords();
    }
}
