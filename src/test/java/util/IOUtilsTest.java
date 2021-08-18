package util;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.assertThat;

public class IOUtilsTest {

    @Test
    @DisplayName("데이터를 제대로 읽는지 확인")
    public void readData() throws Exception {
        String data = "Hello World";
        StringReader sr = new StringReader(data);
        BufferedReader br = new BufferedReader(sr);
        assertThat(IOUtils.readData(br, data.length())).isEqualTo(data);
    }
}
