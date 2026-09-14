package io.github.unerdarks.repositories;

import io.github.unerdarks.models.Account;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AccountRepository {

    private final List<Account> accounts = new ArrayList<>();

    public Account save (Account account){
        Optional<Account> existingAccount = findById(account.getId());
        existingAccount.ifPresent(accounts::remove);

        accounts.add(account);
        return account;
    }

    public Optional<Account> findById(UUID id){
        return accounts.stream()
                .filter(account -> account.getId().equals(id))
                .findFirst();
    }

    public List<Account> findAll() {
        return List.copyOf(accounts);
    }

    public void deleteById(UUID id){
        Optional<Account> account = findById(id);
        account.ifPresent(accounts::remove);
    }

}
