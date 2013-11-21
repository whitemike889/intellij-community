/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.util;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.util.io.URLUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UrlImpl implements Url {
  private String externalForm;
  private UrlImpl withoutParameters;

  @Nullable
  private final String scheme;

  private final String authority;

  private final String path;
  private String decodedPath;

  private final String parameters;

  public UrlImpl(@Nullable String path) {
    this(null, null, path, null);
  }

  public UrlImpl(@NotNull String scheme, @Nullable String authority, @Nullable String path) {
    this(scheme, authority, path, null);
  }

  public UrlImpl(@Nullable String scheme, @Nullable String authority, @Nullable String path, @Nullable String parameters) {
    this.scheme = scheme;
    this.authority = StringUtil.nullize(authority);
    this.path = StringUtil.isEmpty(path) ? "/" : path;
    this.parameters = StringUtil.nullize(parameters);
  }

  @NotNull
  @Override
  public String getPath() {
    if (decodedPath == null) {
      decodedPath = URLUtil.unescapePercentSequences(path);
    }
    return decodedPath;
  }

  @Nullable
  @Override
  public String getScheme() {
    return scheme;
  }

  @Override
  @Nullable
  public String getAuthority() {
    return authority;
  }

  @Override
  public boolean isInLocalFileSystem() {
    return StandardFileSystems.FILE_PROTOCOL.equals(scheme);
  }

  @Nullable
  @Override
  public String getParameters() {
    return parameters;
  }

  @Override
  public String toDecodedForm() {
    StringBuilder builder = new StringBuilder();
    if (scheme != null) {
      builder.append(scheme).append(URLUtil.SCHEME_SEPARATOR);
    }
    if (authority != null) {
      builder.append(authority);
    }
    builder.append(getPath());
    if (parameters != null) {
      builder.append(parameters);
    }
    return builder.toString();
  }

  @Override
  @NotNull
  public String toExternalForm() {
    if (externalForm != null) {
      return externalForm;
    }

    String result = Urls.toUriWithoutParameters(this).toASCIIString();
    if (parameters != null) {
      result += parameters;
    }
    externalForm = result;
    return result;
  }

  @Override
  @NotNull
  public Url trimParameters() {
    if (parameters == null) {
      return this;
    }
    else if (withoutParameters == null) {
      withoutParameters = new UrlImpl(scheme, authority, path, null);
    }
    return withoutParameters;
  }

  @Override
  public String toString() {
    return toExternalForm();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UrlImpl)) {
      if (o instanceof Url && isInLocalFileSystem()) {
        Url url = (Url)o;
        if (url.isInLocalFileSystem()) {
          return url.getPath().equals(path);
        }
      }
      return false;
    }

    UrlImpl url = (UrlImpl)o;
    if (scheme == null ? url.scheme == null : !scheme.equals(url.scheme)) {
      return false;
    }
    if (authority == null ? url.authority != null : !authority.equals(url.authority)) {
      return false;
    }
    if (!getPath().equals(url.getPath())) {
      return false;
    }
    return parameters == null ? url.parameters == null : parameters.equals(url.parameters);
  }

  @Override
  public boolean equalsIgnoreParameters(@Nullable Url url) {
    return url != null && equals(url.trimParameters());
  }

  @Override
  public int hashCode() {
    int result = scheme == null ? 0 : scheme.hashCode();
    result = 31 * result + (authority != null ? authority.hashCode() : 0);
    String decodedPath = getPath();
    result = 31 * result + decodedPath.hashCode();
    result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
    return result;
  }
}