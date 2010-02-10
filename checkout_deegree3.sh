#!/bin/sh
echo Checking out deegree 3 modules from svn.

svn co http://wald.intevation.org/svn/deegree/deegree3/core/trunk d3_core
svn co http://wald.intevation.org/svn/deegree/deegree3/services/trunk d3_services
svn co http://wald.intevation.org/svn/deegree/deegree3/test/trunk d3_test
svn co http://wald.intevation.org/svn/deegree/deegree3/tools/trunk d3_tools

echo Finished checking out deegree 3 from svn.

