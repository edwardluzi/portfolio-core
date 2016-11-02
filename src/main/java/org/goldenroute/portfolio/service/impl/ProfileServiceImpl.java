package org.goldenroute.portfolio.service.impl;

import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.goldenroute.portfolio.model.Binding;
import org.goldenroute.portfolio.model.Profile;
import org.goldenroute.portfolio.repository.BindingRepository;
import org.goldenroute.portfolio.repository.ProfileRepository;
import org.goldenroute.portfolio.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;

public class ProfileServiceImpl implements ProfileService
{
    @Autowired
    private BindingRepository bindingRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public Profile findOne(Long id)
    {
        return profileRepository.findOne(id);
    }

    @Override
    public Binding createBinding(Profile profile)
    {
        Binding binding = new Binding();

        binding.setProfileId(profile.getId());
        binding.setCreatedAt(Calendar.getInstance().getTimeInMillis());
        binding.setExpireSeconds(300);

        int parameter = generateParameter();

        binding.setParameter(parameter);

        bindingRepository.save(binding);

        return binding;
    }

    @Override
    public Binding createBinding(String openId)
    {
        Binding binding = new Binding();

        binding.setOpenId(openId);
        binding.setCreatedAt(Calendar.getInstance().getTimeInMillis());
        binding.setExpireSeconds(300);

        int parameter = generateParameter();

        binding.setParameter(parameter);

        bindingRepository.save(binding);

        return binding;
    }

    @Override
    public boolean bindWechat(String openId, Integer parameter)
    {
        List<Binding> findings = bindingRepository.findByParameter(parameter);

        if (findings == null || findings.size() != 1)
        {
            return false;
        }

        Binding binding = findings.get(0);

        if (binding.getCreatedAt() + binding.getExpireSeconds() * 1000000L < Calendar.getInstance().getTimeInMillis())
        {
            return false;
        }

        Profile profile = profileRepository.findOne(binding.getProfileId());

        if (profile == null)
        {
            return false;
        }

        profile.setWechatId(openId);

        profileRepository.save(profile);

        return true;
    }

    @Override
    public boolean bindWechat(Profile profile, Integer parameter)
    {
        List<Binding> findings = bindingRepository.findByParameter(parameter);

        if (findings == null || findings.size() != 1)
        {
            return false;
        }

        Binding binding = findings.get(0);

        if (binding.getCreatedAt() + binding.getExpireSeconds() * 1000000L < Calendar.getInstance().getTimeInMillis())
        {
            return false;
        }

        profile.setWechatId(binding.getOpenId());

        profileRepository.save(profile);

        return true;
    }

    private Integer generateParameter()
    {
        int parameter = 0;
        Random random = new Random(Calendar.getInstance().getTimeInMillis());

        while (true)
        {
            parameter = 10000000 + (int) (random.nextFloat() * 90000000);

            List<Binding> findings = bindingRepository.findByParameter(parameter);

            if (findings == null || findings.size() == 0)
            {
                break;
            }
        }

        return parameter;
    }
}
