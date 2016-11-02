package org.goldenroute.portfolio.repository;

import java.util.List;

import org.goldenroute.portfolio.model.Binding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BindingRepository extends JpaRepository<Binding, Long>
{
    List<Binding> findByParameter(Integer parameter);
}
