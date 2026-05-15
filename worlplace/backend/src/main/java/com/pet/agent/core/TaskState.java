package com.pet.agent.core;

/**
 * Task lifecycle states inspired by A2A protocol's 9-state machine.
 */
public enum TaskState {

    SUBMITTED("已提交"),
    WORKING("执行中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    CANCELLED("已取消"),
    INPUT_REQUIRED("等待用户输入"),
    REJECTED("已拒绝"),
    WAITING_DEPENDENCY("等待依赖");

    private final String label;

    TaskState(String label) { this.label = label; }

    public String getLabel() { return label; }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REJECTED;
    }

    public boolean canTransitionTo(TaskState target) {
        switch (this) {
            case SUBMITTED:
                return target == WORKING || target == REJECTED || target == CANCELLED;
            case WAITING_DEPENDENCY:
                return target == WORKING || target == CANCELLED;
            case WORKING:
                return target == COMPLETED || target == FAILED
                    || target == INPUT_REQUIRED || target == CANCELLED;
            case INPUT_REQUIRED:
                return target == WORKING || target == CANCELLED;
            default:
                return false;
        }
    }
}
