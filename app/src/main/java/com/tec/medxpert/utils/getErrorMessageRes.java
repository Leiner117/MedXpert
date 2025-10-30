package com.tec.medxpert.utils;

import com.tec.medxpert.R;

public class getErrorMessageRes {
    public static int getErrorMessageRes(Throwable error) {
        if (error == null || error.getMessage() == null) return R.string.unknown_error;

        switch (error.getMessage()) {
            case "error_account_deleted":
                return R.string.error_account_deleted;
            case "error_registered_with_email":
                return R.string.error_email_registered;
            case "error_user_null":
                return R.string.error_user_null;
            default:
                return R.string.unknown_error;
        }
    }
}
