package com.springboot.architectural.controller;

import com.springboot.architectural.dto.VNPayRequestPayDTO;
import com.springboot.architectural.entity.Movie_Buy;
import com.springboot.architectural.entity.PaymentRedis;
import com.springboot.architectural.payload.ResponseData;
import com.springboot.architectural.repository.MovieBuyRepository;
import com.springboot.architectural.repository.RedisRepository;
import com.springboot.architectural.util.Config;
import com.springboot.architectural.util.EmailUtil;
import feign.FeignException;
import jakarta.annotation.Nullable;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
@RestController
@RequestMapping("api/payment/vnpay")

public class VNPayController  {
    @Autowired
    private IFeignClient feignClient;
    @Autowired
    private RedisRepository redisRepository;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    MovieBuyRepository movieBuyRepository;

    @PostMapping("/create")
    public ResponseEntity<?> create( HttpServletRequest request, @RequestBody VNPayRequestPayDTO vnPayRequestPayDTO) throws UnsupportedEncodingException {
        String bearerToken = request.getHeader("Authorization");
        try {
            String response = feignClient.sendToken(bearerToken);
        } catch (FeignException exception) {
            String error = new String(exception.responseBody().get().array());
            return ResponseEntity.status(401).body(error);
        }
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String orderType = "other";
        long amount =  vnPayRequestPayDTO.getAmount() * 100;
        String bankCode = vnPayRequestPayDTO.getBankCode();
        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = Config.getIpAddress(request);

        System.out.println(vnp_IpAddr);

        String vnp_TmnCode = Config.vnp_TmnCode;

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");

        if (bankCode != null && !bankCode.isEmpty()) {
            vnp_Params.put("vnp_BankCode", bankCode);
        }
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnPayRequestPayDTO.getUsername());
        vnp_Params.put("vnp_OrderType", orderType);

        String locate =vnPayRequestPayDTO.getLanguage();

        if (locate != null && !locate.isEmpty()) {
            vnp_Params.put("vnp_Locale", locate);
        } else {
            vnp_Params.put("vnp_Locale", "vn");
        }
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        List fieldNames = new ArrayList(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = (String) itr.next();
            String fieldValue = (String) vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                //Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                //Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;
        System.out.println(paymentUrl);
        redisRepository.save(new PaymentRedis(vnPayRequestPayDTO.getUsername(),vnPayRequestPayDTO.getEmail(), vnPayRequestPayDTO.getMovieId(),vnPayRequestPayDTO.getMovieName(),vnp_TxnRef));
        ResponseData responseData = new ResponseData();
        responseData.setData(paymentUrl);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    @GetMapping(value = "/return", produces = MediaType.TEXT_HTML_VALUE)
    public String success(@RequestParam Integer vnp_Amount, @RequestParam(defaultValue = "0") String vnp_ResponseCode, @RequestParam String vnp_TxnRef, @RequestParam String vnp_OrderInfo) throws MessagingException {
        PaymentRedis paymentRedis =  redisRepository.findPaymentRedisById(vnp_TxnRef, vnp_OrderInfo);
        String responseData;
        if (Objects.equals(vnp_ResponseCode, "00"))
        {
            System.out.println("SUCCESS");
            Movie_Buy movieBuy = new Movie_Buy();
            movieBuy.setTime(new Date());
            movieBuy.setMovieId(paymentRedis.getMovieId());
            movieBuy.setUsername(paymentRedis.getUsername());
            System.out.println(movieBuy);
            movieBuyRepository.save(movieBuy);
            emailUtil.sendEmail(paymentRedis.getEmail(),vnp_Amount,paymentRedis.getMovieName(), vnp_ResponseCode);
            responseData = "<html>\n" +
                "<head>\n" +
                "    <link href=\"https://fonts.googleapis.com/css?family=Nunito+Sans:400,400i,700,900&display=swap\" rel=\"stylesheet\">\n" +
                "</head>\n" +
                "<style>\n" +
                "    body {\n" +
                "      text-align: center;\n" +
                "      padding: 40px 0;\n" +
                "      background: #EBF0F5;\n" +
                "    }\n" +
                "      h1 {\n" +
                "        color: #88B04B;\n" +
                "        font-family: \"Nunito Sans\", \"Helvetica Neue\", sans-serif;\n" +
                "        font-weight: 900;\n" +
                "        font-size: 40px;\n" +
                "        margin-bottom: 10px;\n" +
                "      }\n" +
                "      p {\n" +
                "        color: #404F5E;\n" +
                "        font-family: \"Nunito Sans\", \"Helvetica Neue\", sans-serif;\n" +
                "        font-size:20px;\n" +
                "        margin: 0;\n" +
                "      }\n" +
                "    i {\n" +
                "      color: #9ABC66;\n" +
                "      font-size: 100px;\n" +
                "      line-height: 200px;\n" +
                "      margin-left:-15px;\n" +
                "    }\n" +
                "    .card {\n" +
                "      background: white;\n" +
                "      padding: 60px;\n" +
                "      border-radius: 4px;\n" +
                "      box-shadow: 0 2px 3px #C8D0D8;\n" +
                "      display: inline-block;\n" +
                "      margin: 0 auto;\n" +
                "    }\n" +
                "</style>\n" +
                "<body>\n" +
                "<div class=\"card\">\n" +
                "    <div style=\"border-radius:200px; height:200px; width:200px; background: #F8FAF5; margin:0 auto;\">\n" +
                "        <i class=\"checkmark\">✓</i>\n" +
                "    </div>\n" +
                "    <h1>Success</h1>\n" +
                "    <p>We received your purchase request;<br/> we'll be in touch shortly!</p>\n" +
                "</div>\n" +
                "</body>\n" +
                "</html>";
        }
        else {
            System.out.println("Failed");
            emailUtil.sendEmail(paymentRedis.getEmail(),vnp_Amount,paymentRedis.getMovieName(), vnp_ResponseCode);
            responseData = responseData = "<html>\n" +
                    "<head>\n" +
                    "    <link href=\"https://fonts.googleapis.com/css?family=Nunito+Sans:400,400i,700,900&display=swap\" rel=\"stylesheet\">\n" +
                    "</head>\n" +
                    "<style>\n" +
                    "    body {\n" +
                    "      text-align: center;\n" +
                    "      padding: 40px 0;\n" +
                    "      background: #EBF0F5;\n" +
                    "    }\n" +
                    "      h1 {\n" +
                    "        color: #88B04B;\n" +
                    "        font-family: \"Nunito Sans\", \"Helvetica Neue\", sans-serif;\n" +
                    "        font-weight: 900;\n" +
                    "        font-size: 40px;\n" +
                    "        margin-bottom: 10px;\n" +
                    "      }\n" +
                    "      p {\n" +
                    "        color: #404F5E;\n" +
                    "        font-family: \"Nunito Sans\", \"Helvetica Neue\", sans-serif;\n" +
                    "        font-size:20px;\n" +
                    "        margin: 0;\n" +
                    "      }\n" +
                    "    i {\n" +
                    "      color: #9ABC66;\n" +
                    "      font-size: 100px;\n" +
                    "      line-height: 200px;\n" +
                    "      margin-left:-15px;\n" +
                    "    }\n" +
                    "    .card {\n" +
                    "      background: white;\n" +
                    "      padding: 60px;\n" +
                    "      border-radius: 4px;\n" +
                    "      box-shadow: 0 2px 3px #C8D0D8;\n" +
                    "      display: inline-block;\n" +
                    "      margin: 0 auto;\n" +
                    "    }\n" +
                    "</style>\n" +
                    "<body>\n" +
                    "<div class=\"card\">\n" +
                    "    <div style=\"border-radius:200px; height:200px; width:200px; background: #f0adad; margin:0 auto;\">\n" +
                    "        <i class=\"checkmark\">X</i>\n" +
                    "    </div>\n" +
                    "    <h1>Payment Failed</h1>\n" +
                    "</div>\n" +
                    "</body>\n" +
                    "</html>";
        }

        return responseData;
    }
}
