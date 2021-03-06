package org.apereo.cas.config;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.GrouperMultifactorAuthenticationPolicyEventResolver;
import org.apereo.cas.web.flow.resolver.CasDelegatingWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.AbstractCasWebflowEventResolver;
import org.apereo.cas.web.flow.resolver.impl.NoOpCasWebflowEventResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.CookieGenerator;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * This is {@link GrouperMultifactorAuthenticationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Configuration("grouperMultifactorAuthenticationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnClass(value = CasWebflowEventResolver.class)
public class GrouperMultifactorAuthenticationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrouperMultifactorAuthenticationConfiguration.class);

    @Autowired
    @Qualifier("centralAuthenticationService")
    private CentralAuthenticationService centralAuthenticationService;

    @Autowired
    @Qualifier("defaultAuthenticationSystemSupport")
    private AuthenticationSystemSupport authenticationSystemSupport;

    @Autowired
    @Qualifier("defaultTicketRegistrySupport")
    private TicketRegistrySupport ticketRegistrySupport;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Autowired
    @Qualifier("warnCookieGenerator")
    private CookieGenerator warnCookieGenerator;

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    @Qualifier("initialAuthenticationAttemptWebflowEventResolver")
    private CasDelegatingWebflowEventResolver initialAuthenticationAttemptWebflowEventResolver;

    @Autowired
    @Qualifier("multifactorAuthenticationProviderSelector")
    private MultifactorAuthenticationProviderSelector multifactorAuthenticationProviderSelector;

    @Autowired
    @Qualifier("authenticationRequestServiceSelectionStrategies")
    private List<AuthenticationRequestServiceSelectionStrategy> authenticationRequestServiceSelectionStrategies;

    @Bean
    @RefreshScope
    public CasWebflowEventResolver grouperMultifactorAuthenticationWebflowEventResolver() {

        final AbstractCasWebflowEventResolver r;
        if (StringUtils.isNotBlank(casProperties.getAuthn().getMfa().getGrouperGroupField())) {
            r = new GrouperMultifactorAuthenticationPolicyEventResolver();
            LOGGER.debug("Activating MFA event resolver based on Grouper groups...");
        } else {
            r = new NoOpCasWebflowEventResolver();
        }

        r.setAuthenticationSystemSupport(authenticationSystemSupport);
        r.setCentralAuthenticationService(centralAuthenticationService);
        r.setMultifactorAuthenticationProviderSelector(multifactorAuthenticationProviderSelector);
        r.setServicesManager(servicesManager);
        r.setTicketRegistrySupport(ticketRegistrySupport);
        r.setWarnCookieGenerator(warnCookieGenerator);
        r.setAuthenticationRequestServiceSelectionStrategies(authenticationRequestServiceSelectionStrategies);
        return r;
    }


    @PostConstruct
    public void initConfig() {
        this.initialAuthenticationAttemptWebflowEventResolver.addDelegate(grouperMultifactorAuthenticationWebflowEventResolver());
    }

}
