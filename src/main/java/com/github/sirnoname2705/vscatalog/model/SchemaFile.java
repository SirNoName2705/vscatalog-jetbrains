package com.github.sirnoname2705.vscatalog.model;

import com.github.sirnoname2705.vscatalog.DependencyResolver;
import com.github.sirnoname2705.vscatalog.PluginInitializer;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.SchemaType;
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaFile extends JsonFile implements JsonSchemaFileProvider {
    private static final Pattern schemaVersionPattern = Pattern.compile(
            com.github.sirnoname2705.vscatalog.settings.Util.SCHEMA_VERSION_PATTERN_STRING);
    private final String name;
    private final String description;
    private final String VSVersion = "1.20.5";
    private JsonSchemaVersion schemaVersion;
    private List<FileMatch> fileMatch = new ArrayList<>();

    public SchemaFile(String url, String name, String description) {
        super(url);
        this.name = name;
        this.description = description;
//        this.virtualFile = LoadOrFetchFile();
//        updateFromRemoteAsync();
    }

    public SchemaFile(String url, String name, String description, Collection<String> fileMatch) {
        this(url, name, description);
        addAllFileMatches(fileMatch);
    }

    public SchemaFile(String url, String name, String description, List<String> fileMatch, boolean autoDownload) {
        super(url, autoDownload);
        this.name = name;
        this.description = description;
        addAllFileMatches(fileMatch);
    }


    // This is kind of stupid and need refactoring
    private VirtualFile LoadOrFetchFile() {
        return this.getVirtualFile();
    }

    /*
    This is the function that is called after the file is successfully downloaded
     */
    public void afterDownloadAction() {
        super.afterDownloadAction();
        DependencyResolver.resolveAndDownloadUrls(this);
    }

    public void downloadJson() {
        if (super.downloadStarted) {
            return;
        }
        this.ensureDirExists();
        PluginInitializer.contentProvider.downloadContent(getExternalUrl(), getLocalPath(), this::afterDownloadAction);
        this.downloadStarted = true;
    }

    private JsonSchemaVersion GetSchemaVersionFromFile() {
        if (this.schemaVersion != null) {
            return this.schemaVersion;
        }
        if (this.getVirtualFile() == null) {
            return JsonSchemaVersion.SCHEMA_4;
        }
        JsonSchemaVersion version;
        var content = getContent();
        Matcher matcher = schemaVersionPattern.matcher(content);
        if (matcher.find()) {
            String schemaVersionString = matcher.group(1);
            version = JsonSchemaVersion.byId(schemaVersionString);
            return version;
        }
        return JsonSchemaVersion.SCHEMA_4;
    }


    @Override
    public boolean isAvailable(@NotNull VirtualFile file) {
        if (fileMatch.isEmpty()) {
            return false;
        }
        if (file.getExtension() == null || !"json".equals(file.getExtension())) {
            return false;
        }
        for (FileMatch match : fileMatch) {
            if (match.isMatch(file.getPath())) {
                return true;
            }
        }
        return false;

    }

    private void addAllFileMatches(Collection<String> fileMatches) {
        this.fileMatch.addAll(FileMatch.fromCollection(fileMatches));
    }

    @Override
    public @NotNull @Nls String getName() {
        return this.name;
    }

    @Override
    public @Nullable VirtualFile getSchemaFile() {
        return LoadOrFetchFile();
    }

    @Override
    public @NotNull SchemaType getSchemaType() {
        return SchemaType.embeddedSchema;
    }

    @Override
    public JsonSchemaVersion getSchemaVersion() {
        return GetSchemaVersionFromFile();
    }

    @Override
    public @Nullable @Nls String getThirdPartyApiInformation() {
        return GetVintageStoryVersion();
    }

    private @Nullable @Nls String GetVintageStoryVersion() {
        return VSVersion;
    }

    @Override
    public boolean isUserVisible() {
        return true;
    }

    @Override
    public @NotNull @NlsContexts.ListItem String getPresentableName() {
        return getName().replace(".json", "");
    }

    @Override
    public @Nullable @NonNls String getRemoteSource() {
        return this.url;
    }

    @Override
    public String toString() {
        return "SchemaFile{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", fileMatch=" + fileMatch +
                '}';
    }

}
