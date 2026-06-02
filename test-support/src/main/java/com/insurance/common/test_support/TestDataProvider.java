package com.insurance.common.test_support;

import java.util.function.Consumer;

/**
 * Interface to implement for provisioning an entity with default data.
 * @param <Entity> the type of the entity
 */
public interface TestDataProvider<Entity> extends Consumer<Entity> {

    Class<Entity> getEntityClass();

}
