package gdg.oauthgoogleloginexample.service;

import com.google.gson.Gson;
import gdg.oauthgoogleloginexample.domain.Role;
import gdg.oauthgoogleloginexample.domain.User;
import gdg.oauthgoogleloginexample.dto.TokenDto;
import gdg.oauthgoogleloginexample.dto.UserInfo;
import gdg.oauthgoogleloginexample.jwt.TokenProvider;
import gdg.oauthgoogleloginexample.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.Principal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleLoginService {

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;

    public String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, String> params = Map.of(
                "code", code,
                "scope", "https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email",
                "client_id", googleClientId,
                "client_secret", googleClientSecret,
                "redirect_uri", googleRedirectUri,
                "grant_type", "authorization_code"
        );

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(GOOGLE_TOKEN_URL, params, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return new Gson()
                    .fromJson(responseEntity.getBody(), TokenDto.class)
                    .getAccessToken();
        }
        throw new RuntimeException("Failed to get Google access token.");
    }

    public TokenDto loginOrSignUp(String googleAccessToken) {
        UserInfo userInfo = getUserInfo(googleAccessToken);

        if (!Boolean.TRUE.equals(userInfo.getVerifiedEmail())) {
            throw new RuntimeException("Unverified email user.");
        }

        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(userInfo.getEmail())
                        .name(userInfo.getName())
                        .profileUrl(userInfo.getPictureUrl())
                        .role(Role.USER)
                        .build()));

        return TokenDto.builder()
                .accessToken(tokenProvider.createAccessToken(user))
                .build();
    }

    private UserInfo getUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
        ResponseEntity<String> responseEntity = restTemplate.exchange(requestEntity, String.class);

        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return new Gson().fromJson(responseEntity.getBody(), UserInfo.class);
        }

        throw new RuntimeException("Failed to fetch Google user info.");
    }

    public User test(Principal principal) {
        return userRepository.findById(Long.parseLong(principal.getName()))
                .orElseThrow(() -> new RuntimeException("User not found."));
    }
}
