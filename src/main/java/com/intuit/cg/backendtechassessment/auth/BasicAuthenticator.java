package com.intuit.cg.backendtechassessment.auth;

import com.google.common.base.Charsets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.inject.Inject;
import com.intuit.cg.backendtechassessment.config.AssessmentConfig;
import com.intuit.cg.backendtechassessment.data.entities.UserEntity;
import com.intuit.cg.backendtechassessment.data.managers.UserManager;
import com.intuit.cg.backendtechassessment.metrics.MetricsBundle;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import io.prometheus.client.Histogram.Timer;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Just go with a basic authentication. Don't need to do anything too fancy.
 */
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class BasicAuthenticator implements Authenticator<BasicCredentials, User> {
    private final AssessmentConfig config;
    private final UserManager userManager;
    private final MetricsBundle metricsBundle;

    @Override
    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
        try (@SuppressWarnings("unused") Timer timer = metricsBundle.getAuthenticationLatency().startTimer()) {
            // TODO We should make sure that the flag is not enabled when in production
            // Provides a way to disable auth.
            if (config.isAuthDisabled()) {
                return Optional.of(new User("bypassedUserId", "bypassedUsername"));
            }

            UserEntity userEntity = userManager.getUserByName(credentials.getUsername());

            if (userEntity != null) {
                // TODO Make more secure! No salt, it's bad!
                HashCode hashedPassword = Hashing.sha256().hashBytes(credentials.getPassword().getBytes(Charsets.UTF_8));

                if (hashedPassword.toString().equalsIgnoreCase(userEntity.getHashedPassword())) {
                    metricsBundle.getAuthentication().labels(MetricsBundle.SUCCESS).inc();
                    return Optional.of(new User(userEntity.getId(), userEntity.getName()));
                }
            }

            metricsBundle.getAuthentication().labels(MetricsBundle.FAILURE).inc();
            return Optional.empty();
        }
    }
}
