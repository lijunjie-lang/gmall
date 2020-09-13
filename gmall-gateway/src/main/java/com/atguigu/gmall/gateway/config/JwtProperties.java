package com.atguigu.gmall.gateway.config;

import com.atguigu.gmall.common.utils.RsaUtils;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.io.File;
import java.security.PrivateKey;
import java.security.PublicKey;

@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtProperties {

    private String pubKeyPath;
    private String cookieName;

    private PublicKey publicKey;
    private PrivateKey privateKey;

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
