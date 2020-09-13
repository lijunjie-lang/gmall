package com.atguigu.gmall.cart.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String pubKeyPath;
    private String cookieName;
    private String userKeyName;
    private Integer expire;

    private PublicKey publicKey;

    @PostConstruct
    public void init(){
        try {
            File pubFile = new File(pubKeyPath);
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
