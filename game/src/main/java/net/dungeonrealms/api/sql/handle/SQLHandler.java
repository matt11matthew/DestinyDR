package net.dungeonrealms.api.sql.handle;

import lombok.Getter;
import net.dungeonrealms.api.sql.SQLDatabase;
import net.dungeonrealms.api.sql.enumeration.EnumSQLPurpose;
import net.dungeonrealms.common.awt.SuperHandler;

/**
 * Created by Giovanni on 29-10-2016.
 * <p>
 * This file is part of the Dungeon Realms project.
 * Copyright (c) 2016 Dungeon Realms;www.vawke.io / development@vawke.io
 */
public class SQLHandler implements SuperHandler.Handler
{
    @Getter
    private SQLDatabase sqlDatabase;

    private boolean locked;

    @Override
    public void prepare()
    {
        if (!locked)
            this.sqlDatabase = new SQLDatabase(null, null, null, null, null, EnumSQLPurpose.ITEM); // TODO
        this.locked = true;
    }
}