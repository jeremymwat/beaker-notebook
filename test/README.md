<!--
    Copyright 2014 TWO SIGMA OPEN SOURCE, LLC

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

           http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->

Beaker Testing
==========
Enclosed are a set of tests powered by protractor.

### Installing

* just execute './runner'

### Running the tests

* just execute './runner'

### Implementing new tests
* Create a new test file in tests/ folder
* Add the new file path to protractorConf.js

### Manual run of the tests (for new test development)

* start beaker in one shell
* run './node_modules/protractor/bin/webdriver-manager start' in a second shell
* run './node_modules/protractor/bin/protractor protractorConf.js ' in a third shell
