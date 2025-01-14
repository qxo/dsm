package com.github.mfatihercik.dsb;

import com.github.mfatihercik.dsb.configloader.ConfigConstants;
import com.github.mfatihercik.dsb.model.ParsingElement;
import com.github.mfatihercik.dsb.utils.PathUtils;
import com.github.mfatihercik.dsb.utils.StringUtils;

import static com.github.mfatihercik.dsb.configloader.ConfigConstants.PATH_SEPARATOR;

public abstract class AbstractPathGenerator implements PathGenerator {
    public static final String DOUBLE_DOT = "..";

    public static final String DOUBLE_SLASH = ConfigConstants.PATH_SEPARATOR.concat("{2,}");

    public static final String TRIPLE_DOT = "(\\.) {3,}";

    @Override
    public String generatePath(ParsingElement parsingElement) {
        String tagAbsolutePath = parsingElement.getAbsolutePath();
        if (tagAbsolutePath == null) {
            String builder = "";
            boolean isRelativePath = false;
            String tagParentPath = getTagParentPath(parsingElement);
            if (tagParentPath != null && tagParentPath.startsWith(PATH_SEPARATOR)) {

                builder = builder.concat(tagParentPath);
                isRelativePath = true;
            }
            String tagPath = getTagPath(parsingElement);
            if (!isRelativePath && tagPath.startsWith(PATH_SEPARATOR)) {
                isRelativePath = true;
            }
            if (isRelativePath) {
                return builder;
            }

            builder = getParentElementPath(parsingElement);
            if (tagParentPath != null) {
                builder = concatPaths(builder, tagParentPath);
            }
            return normalize(builder);
        } else {
            return tagAbsolutePath;
        }

    }

    private String getParentElementPath(ParsingElement parsingElement) {
        String tagParentPath = getTagParentPath(parsingElement);
        String tagPath = getTagPath(parsingElement);

        ParsingElement parent = parsingElement.getParentElement();

        ParsingElement relativeParent = findRelativeParentElement(parent, tagParentPath);

        boolean isRelativePath = relativeParent != parent;
        if (!isRelativePath)
            relativeParent = findRelativeParentElement(parent, tagPath);

        parent = relativeParent;

        String path = "";
        if (parent != null) {
            String generatePath = generatePath(parent);
            String parentTagPath = getTagPath(parent);
            path = concatPaths(generatePath, parentTagPath);
        }
        return path;
    }

    @Override
    public String concatPaths(String parentPath, String child) {
        String path = parentPath;
        if (child != null)
            path = parentPath.concat(PATH_SEPARATOR).concat(normalize(removeDuplicateCharacters(child)));

        return normalize(path);
    }

    public String removeDuplicateCharacters(String path) {
        while (path.startsWith("...")) {
            path = path.substring(1);
        }
        path = path.replaceAll(DOUBLE_SLASH, ConfigConstants.PATH_SEPARATOR);
        return path;
    }

    public String normalize(String path) {
        while (path.startsWith("./")) {
            path = path.substring(2);
        }
        while (path.startsWith("../")) {
            path = path.substring(3);
        }
        String normalized = StringUtils.removeEnd(path.replaceAll(DOUBLE_SLASH, PATH_SEPARATOR), PATH_SEPARATOR);
        if (StringUtils.isBlank(normalized))
            return PATH_SEPARATOR;
        else
            return normalized;
    }

    protected ParsingElement findRelativeParentElement(ParsingElement parent, String path) {
        if (path != null && path.startsWith("..")) {
            String normalizedPAth = removeDuplicateCharacters(path);
            String[] paths = normalizedPAth.split(PATH_SEPARATOR);
            for (String pathPart : paths) {
                if (PathUtils.DOUBLE_DOT.equals(pathPart)) {
                    if (parent == null)
                        break;
                    parent = parent.getParentElement();
                } else {
                    break;
                }
            }
        }
        return parent;
    }

}
