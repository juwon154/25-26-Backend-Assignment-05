package gdg.oauthgoogleloginexample.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@AllArgsConstructor
public class TokenDto {

    @SerializedName("access_token")
    private String accessToken;
}
