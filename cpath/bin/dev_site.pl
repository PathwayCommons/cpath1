#!/usr/bin/perl
# Script to checkout cpath, and generate the Maven Developer Site
# Author:  Ethan Cerami (cerami@cbio.mskcc.org).

use strict;

# CD to the temp directory
chdir "/home/cerami/temp" or die;

# Delete everything within temp to do a clean build
system "rm -rf *";

# Check out all code
system "cvs checkout -P sander/cpath";
chdir "sander/cpath" or die;

# Run Prepare_dev_site target
system "ant prepare_dev_site";

# Run Ant Eater Functional Tests
system "anteater -f ant_eater/tests.xml";

# Run Maven
system "maven site:generate";
system "maven pdf";
system "maven site:fsdeploy";

# Create source distribition
system "tar -cvf cpath.tar *";
system "gzip cpath.tar";
system "cp cpath.tar.gz /var/www/dev_site/cpath";

# Move Ant Eater Reports
chdir "/var/www/dev_site/cpath/reports" or die;
system "rm -rf *";
chdir "/home/cerami/temp/sander/cpath";
system "mv ant_eater/reports/* /var/www/dev_site/cpath/reports/";
