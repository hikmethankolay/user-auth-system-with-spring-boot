---
title: 'ADR 6: Javadoc for the Java reference'
description: Why the Java reference moved from Doxygen to Javadoc, and how the documentation gate works.
---

**Status:** accepted (2.9.2), supersedes [ADR 4](../0004-doxygen/)

## Context

[ADR 4](../0004-doxygen/) generated the Java reference with Doxygen. That needed a pinned Doxygen binary in CI and on
every contributor's machine (plus Perl on Windows), and a Perl input filter for Java syntax Doxygen does not parse,
records and annotation defaults, which had to keep up with each new Java release. Javadoc ships with the JDK the
project already requires and understands all of Java.

## Decision

`maven-javadoc-plugin` generates the reference (`scripts/docs.sh`, i.e. `./mvnw javadoc:javadoc`) into
`target/reports/apidocs`, with `doclint` set to `all` and `failOnWarnings`, so any undocumented or malformed comment
fails the build. It documents public, protected and package-private members, as Doxygen did, and `linksource` keeps
the browsable source.

## Consequences

- No extra tool to install: the JDK and the Maven wrapper are enough, on every platform.
- Javadoc reports an implicit default constructor as undocumented, so classes that would have one declare it with a
  comment.
- The reference no longer uses `README.md` as its main page; the package summaries in `package-info.java` are the
  entry point, and the documentation site links the reference.
