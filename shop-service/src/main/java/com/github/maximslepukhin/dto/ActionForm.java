package com.github.maximslepukhin.dto;

import com.github.maximslepukhin.enums.ActionType;
import lombok.Data;

@Data
public class ActionForm {
    private ActionType action;

    public ActionForm(ActionType action) {
        this.action = action;
    }
}
