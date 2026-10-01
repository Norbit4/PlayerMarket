package pl.norbit.playermarket.config;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ConfigDialog {
    private String title;
    private String textBreak;
    private String textTitle;
    private String textInfo;
    private String backButton;
    private String acceptButton;
}
