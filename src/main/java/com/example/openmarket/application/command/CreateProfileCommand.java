package com.example.openmarket.application.command;

import com.example.openmarket.application.domain.Profile;
import com.example.openmarket.application.domain.Role;

public abstract class CreateProfileCommand {

    public abstract Role getRole();

    public abstract Profile execute();
}
