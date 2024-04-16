package com.springboot.architectural.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VNPayRequestPayDTO {
    private String bankCode;
    private Integer amount;
    private String language;
    private String username;
    private String movieId;
    private String email;
    private String movieName;
}
