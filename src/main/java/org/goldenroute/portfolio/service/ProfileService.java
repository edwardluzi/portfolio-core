package org.goldenroute.portfolio.service;

import org.goldenroute.portfolio.model.Binding;
import org.goldenroute.portfolio.model.Profile;

public interface ProfileService
{
    Profile findOne(Long id);

    Binding createBinding(Profile profile);

    Binding createBinding(String openId);

    boolean bindWechat(String openId, Integer parameter);

    boolean bindWechat(Profile profile, Integer parameter);
}
