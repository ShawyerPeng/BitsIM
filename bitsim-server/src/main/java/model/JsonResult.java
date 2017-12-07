package model;

import com.alibaba.fastjson.JSONObject;

public class JsonResult extends JSONObject {
    public static JsonResult getJsonResult() {
        return new JsonResult().putCode(CodeType.SUCCESS.value());
    }

    public static JsonResult getJsonResult(Object data) {
        return getJsonResult().putData(data);
    }

    private JsonResult putCode(int code) {
        put("code", code);
        return this;
    }

    private JsonResult putData(Object data) {
        put("data", data);
        return this;
    }

    public static enum CodeType {
        SUCCESS(0),
        ERROR(1);
        private int code;

        private CodeType(int code) {
            this.code = code;
        }

        public int value() {
            return code;
        }
    }
}