package com.github.maximslepukhin.payment.dto;

//import com.github.maximslepukhin.intershop.intershop.enums.ActionType;
import com.github.maximslepukhin.payment.enums.ActionType;
import lombok.Data;

@Data
public class ActionForm {
    private ActionType action;

    public ActionForm(ActionType action) {
        this.action = action;
    }
}
