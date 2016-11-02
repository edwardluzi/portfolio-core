package org.goldenroute.portfolio.service;

import org.springframework.beans.factory.InitializingBean;

public interface DatabaseInitializer extends InitializingBean
{
    void config();
}
