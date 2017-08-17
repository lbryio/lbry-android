class ServiceApp(App):
    def build(self):
        from jnius import autoclass

        Intent = autoclass('android.content.Intent')
        LbrynetService = autoclass('io.lbry.lbrynet.LbrynetService')
        #context = autoclass('org.kivy.android.PythonActivity').mActivity

        #LbrynetService.start(context, '')

        # close the activity once the service starts
        # ideally, we should have some form of service control for the activity
        #context.finish()

if __name__ == '__main__':
    ServiceApp().run()
