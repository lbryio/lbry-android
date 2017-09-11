import sys
import StringIO

from twisted.trial.runner import (
    TestLoader,
    TrialRunner
)
from twisted.trial.reporter import VerboseTextReporter
from twisted.plugin import getPlugins, IPlugin
from jnius import autoclass
import tests

str_stream = StringIO.StringIO()

serviceClass = autoclass('io.lbry.lbrynet.LbrynetTestRunnerService')

def update_output_in_activity(str):
    service = serviceClass.serviceInstance
    if service is not None:
        service.broadcastTestRunnerOutput(str)

class AndroidTestReporter(VerboseTextReporter):
    def startTest(self, tm):
        self._write('%s ... ', tm.id())
        super(AndroidTestReporter, self).startTest(tm)
        update_output_in_activity(self._stream.getvalue())

    def addSuccess(self, test):
        super(AndroidTestReporter, self).addSuccess(test)
        update_output_in_activity(self._stream.getvalue())

    def addError(self, *args):
        super(AndroidTestReporter, self).addError(*args)
        update_output_in_activity(self._stream.getvalue())

    def addFailure(self, *args):
        super(AndroidTestReporter, self).addFailure(*args)
        update_output_in_activity(self._stream.getvalue())

    def addSkip(self, *args):
        super(VerboseTextReporter, self).addSkip(*args)
        update_output_in_activity(self._stream.getvalue())

    def addExpectedFailure(self, *args):
        super(AndroidTestReporter, self).addExpectedFailure(*args)
        update_output_in_activity(self._stream.getvalue())

    def addUnexpectedSuccess(self, *args):
        super(AndroidTestReporter, self).addUnexpectedSuccess(*args)
        update_output_in_activity(self._stream.getvalue())

    def stopTest(self, test):
        super(AndroidTestReporter, self).stopTest(test)
        # TODO: Use appendLine here
        self._write("<br>"); # html output
        update_output_in_activity(self._stream.getvalue())


def run():
    loader = TestLoader();
    suite = loader.loadPackage(tests, True)
    runner = TrialRunner(AndroidTestReporter)
    runner.stream = str_stream
    passFail = not runner.run(suite).wasSuccessful()

    print str_stream.getvalue()
    sys.exit(passFail)

if __name__ == '__main__':
    run()
