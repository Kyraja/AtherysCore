package com.atherys.core.utils;

import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.io.File;
import java.io.IOException;

/**
 * An abstract utility class for creating quick and simple configuration classes using an object mapper.
 */
public abstract class PluginConfig {

    private boolean useDefaults = false;

    private ObjectMapper<PluginConfig>.BoundInstance configMapper;
    protected ConfigurationLoader<CommentedConfigurationNode> loader;

    /**
     * This constructor will load all serializable fields ( the ones marked with {@link Setting} and {@link ConfigSerializable} )
     * and then attempt to create a HOCON file in the given directory with the given name.
     * If the file already exists, it will load values from the config file, overriding the defaults. If it does not, it will save to the file with the default values provided.
     * @param directory The directory where the config fille will be saved.
     * @param filename The name of the config file.
     * @throws IOException when either the file or the directory could not be created.
     */
    protected PluginConfig( String directory, String filename ) throws IOException {

        try {
            this.configMapper = ObjectMapper.forObject(this);
        } catch (ObjectMappingException e) {
            e.printStackTrace();
        }

        File configFile = new File( directory + "/" + filename );

        if ( !configFile.exists() ) {
            if ( configFile.getParentFile().mkdirs() && configFile.createNewFile() ) {
                useDefaults = true;
            } else {
                throw new IOException("Failed to create config directory/file.");
            }
        } else useDefaults = false;

        this.loader = HoconConfigurationLoader.builder().setPath( configFile.toPath() ).build();

        if ( useDefaults ) this.save();
        else this.load();
    }

    /**
     * Save the contents of the object mapper to the config file. This will override config values already-present in the file.
     */
    public void save() {
        try {
            SimpleConfigurationNode out = SimpleConfigurationNode.root();
            this.configMapper.serialize(out);
            this.loader.save(out);
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Populate the object mapper with the contents of the config file. This will override any default values.
     */
    public void load() {
        try {
            this.configMapper.populate( this.loader.load() );
        } catch (ObjectMappingException | IOException e) {
            e.printStackTrace();
        }
    }
}
