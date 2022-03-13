package br.com.jcaguiar.cinephiles.company;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    CompanyRepository repo;

    public CompanyEntity loadOrSave(@NotBlank String name) {
        return Optional.ofNullable(repo.findByName(name))
            .orElseGet(() -> newCompany(name));
    }

    private CompanyEntity newCompany(@NotBlank String name) {
        final CompanyEntity companyEntity = CompanyEntity
            .builder().name(name).build();
        return repo.saveAndFlush(companyEntity);
    }

}
