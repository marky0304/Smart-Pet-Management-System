-- 订单表添加退货退款字段
USE smart_pet_system;

ALTER TABLE `orders`
  ADD COLUMN `return_reason` VARCHAR(500) NULL COMMENT '退货原因',
  ADD COLUMN `return_time` DATETIME NULL COMMENT '申请退货时间',
  ADD COLUMN `refund_amount` DECIMAL(10,2) NULL COMMENT '退款金额',
  ADD COLUMN `refund_time` DATETIME NULL COMMENT '退款处理时间',
  ADD COLUMN `refund_notes` VARCHAR(255) NULL COMMENT '退款备注';
