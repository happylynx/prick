package com.github.happylynx.prick.lib.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import java.nio.file.Path;

public class Module extends AbstractModule  {

    public static Injector init() {
        return Guice.createInjector(new Module());
    }

    @Override
    protected void configure() {
        bind(PrickRootProvider.class).in(Singleton.class);
        bind(Path.class).annotatedWith(PrickRoot.class).toProvider(PrickRootProvider.class);
    }
}
