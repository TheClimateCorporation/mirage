package com.climate.mirage.load;

import java.io.IOException;
import java.io.InputStream;

public interface StreamProvider {



    InputStream load() throws IOException;

}
