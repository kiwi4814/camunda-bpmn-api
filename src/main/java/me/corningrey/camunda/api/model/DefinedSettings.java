
package me.corningrey.camunda.api.model;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * DefinedSettings 自定义设置
 *
 * @Description 自定义配置文件Properties对应的Key对象
 */
@Data
@Component
public class DefinedSettings {
    /**
     * login
     */
    @Value("${dd.login.url:''}")
    private String loginUrl;
    @Value("${dd.loginout.url:''}")
    private String loginOutUrl;
    @Value("${dd.login.success.url:''}")
    private String loginSuccessUrl;
    @Value("${dd.login.process.url:''}")
    private String loginProcessingUrl;
    @Value("${dd.login.unauth.url:''}")
    private String unauthorizedUrl;
    @Value("${dd.login.account:''}")
    private String loginAccount;
    @Value("${dd.login.pw:''}")
    private String loginPw;
    @Value("${dd.login.kaptcha:''}")
    private String loginAuthCode;
    /**
     * common
     */
    @Value("${spring.messages.basename:''}")
    private String baseName;
    /**
     * Kaptcha
     */
    @Value("${dd.kaptcha.border:'no'}")
    private String kaptchaBorder;
    @Value("${dd.kaptcha.border.color:''}")
    private String kaptchaBorderColor;
    @Value("${dd.kaptcha.border.thickness:''}")
    private String kaptchaBorderThickness;
    @Value("${dd.kaptcha.producer.impl:''}")
    private String kaptchaProducerImpl;
    @Value("${dd.kaptcha.textproducer.impl:''}")
    private String kaptchaTextProducerImpl;
    @Value("${dd.kaptcha.textproducer.char.string:''}")
    private String kaptchaTextProducerCharString;
    @Value("${dd.kaptcha.textproducer.char.length:''}")
    private String kaptchaTextProducerCharLength;
    @Value("${dd.kaptcha.textproducer.font.names:''}")
    private String kaptchaTextProducerFontNames;
    @Value("${dd.kaptcha.textproducer.font.size:'14'}")
    private String kaptchaTextProducerFontSize;
    @Value("${dd.kaptcha.textproducer.font.color:'blue'}")
    private String kaptchaTextProducerFontColor;
    @Value("${dd.kaptcha.textproducer.char.space:''}")
    private String kaptchaTextProducerCharSpace;
    @Value("${dd.kaptcha.noise.impl:''}")
    private String kaptchaNoiseImpl;
    @Value("${dd.kaptcha.noise.color:''}")
    private String kaptchaNoiseColor;
    @Value("${dd.kaptcha.obscurificator.impl:''}")
    private String kaptchaObscurificatorImpl;
    @Value("${dd.kaptcha.word.impl:''}")
    private String kaptchaWordImpl;
    @Value("${dd.kaptcha.background.impl:''}")
    private String kaptchaBackgroundImpl;
    @Value("${dd.kaptcha.background.clear.from:''}")
    private String kaptchaBackgroundClearFrom;
    @Value("${dd.kaptcha.background.clear.to:''}")
    private String kaptchaBackgroundClearTo;
    @Value("${dd.kaptcha.image.width:''}")
    private String kaptchaImageWidth;
    @Value("${dd.kaptcha.image.height:''}")
    private String kaptchaImageHeight;
    @Value("${dd.kaptcha.session.key:''}")
    private String kaptchaSessionKey;
    @Value("${dd.kaptcha.session.date:''}")
    private String kaptchaSessionDate;

    /**
     * spring security
     */
    @Value("${dd.is.security.shiro:false}")
    private boolean securityShiro;
    @Value("#{'${dd.security.ignored.anon:}'.split(',')}")
    private String[] securityIgnoredAnonArray;
    @Value("#{'${dd.security.ignored.static.anon:/static/**}'.split(',')}")
    private String[] securityIgnoredStaticAnonArray;
    @Value("#{'${dd.security.ignored.authc:}'.split(',')}")
    private String[] securityIgnoredAuthcArray;
    @Value("#{'${dd.interceptor.path:}'.split(',')}")
    private String[] interceptorPaths;
    @Value("${dd.interceptor.validity:false}")
    private boolean interceptorValidity;

    /**
     * Redis
     */
    @Value("${spring.redis.timeout:7200}")
    private String redisTimeOut;

    /**
     * Swagger
     */
    @Value("${dd.swagger.base.package:''}")
    private String swaggerBasePackage;
    @Value("${dd.swagger.title:''}")
    private String swaggerTitle;
    @Value("${dd.swagger.description:''}")
    private String swaggerDescription;
    @Value("${dd.swagger.version:''}")
    private String swaggerVersion;
    @Value("${dd.swagger.license:''}")
    private String swaggerLicense;
    @Value("${dd.swagger.author:''}")
    private String swaggerAuthor;

    /**
     * filter variables
     */
    @Value("${dd.filter.variables:''}")
    private String filterVariables;

    /**
     * 自动完成待办的整体开关
     */
    @Value("${dd.auto.complete:'false'}")
    private String autoComplete;
}
