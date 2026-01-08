package com.example.openmarket.application.command;

import com.example.openmarket.application.domain.Account;
import com.example.openmarket.application.domain.Profile;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateAccountCommand {

    private final String email;
    private final String password;
    private final String name;
    private final Set<CreateProfileCommand> profileCommands;

    public CreateAccountCommand(String email, String password, String name,
                                Set<CreateProfileCommand> profileCommands) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.profileCommands = profileCommands;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public Set<CreateProfileCommand> getProfileCommands() { return profileCommands; }

    public Account execute(){
        Set<Profile> profiles = profileCommands.stream()
                .map(CreateProfileCommand::execute)
                .collect(Collectors.toSet());

        return Account.create(
                this.email,
                this.password,
                this.name,
                profiles
        );
    }
}
