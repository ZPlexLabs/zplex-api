package zechs.zplex.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;

public class GsonConfig {

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create();
    }

}
