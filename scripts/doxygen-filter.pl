#!/usr/bin/env perl
# Doxygen input filter (see FILTER_PATTERNS in Doxyfile). Only Doxygen sees its output; sources are untouched.
#
# Doxygen's Java parser does not understand two Java constructs used in this project:
#   - records: `record Name(components) implements X {` is parsed as a method, so the members in
#     the record body are attached to the wrong element. It is rewritten to `class Name implements X {`;
#     the components stay documented through the @param tags of the record's comment.
#   - annotation member defaults: `String message() default "...";` confuses the parser.
#     The `default <value>` part is dropped.
use strict;
use warnings;

# Doxygen passes the file name as the argument; <> also accepts stdin for manual testing.
local $/;
my $source = <> // '';

# Balanced-parenthesis match, so annotations with arguments inside the record header are handled.
my $parens = qr/(\((?:[^()"]++|"(?:[^"\\]|\\.)*"|(?-1))*\))/;

$source =~ s/\brecord\s+(\w+(?:<[^>]*>)?)\s*$parens/class $1/g;
$source =~ s/(\w+\s*\(\s*\))\s+default\s+[^;]+;/$1;/g;

print $source;
