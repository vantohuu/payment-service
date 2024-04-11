package com.springboot.architectural.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class ResponseData {
    private int status;
    private String desc;
    private Object data;
    private boolean isSuccess;

    public ResponseData() {
        this.status = 200;
        this.isSuccess = true;
    }

    public ResponseData(int status, String desc, Object data) {
        this.status = status;
        this.desc = desc;
        this.data = data;
    }
}
