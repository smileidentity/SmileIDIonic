//
//  SIDSelfieViewController.swift
//  IonicTestApp
//
//  Created by Japhet Ndhlovu on 2020/11/16.
//

import UIKit
import Smile_Identity_SDK

class SIDSelfieViewController: UIViewController,CaptureSelfieDelegate {
    @IBOutlet weak var lblPrompt: UILabel!
    @IBOutlet weak var selfieView: VideoPreviewView!
    var selfieViewControllerDelegate : SelfieViewControllerDelegate?
    var captureSelfie                   : CaptureSelfie?
    var currentTag       : String  = ""
    
    override func viewDidLoad() {
        super.viewDidLoad()

        // Do any additional setup after loading the view.
    }


    /*
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
    }
    */
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        lblPrompt.text = "Put Your Face Inside Oval"
        SelfieCaptureConfig.setMaxFrameTimeout( maxFrameTimeout : 200 )
        captureSelfie = CaptureSelfie()
        captureSelfie?.setup(captureSelfieDelegate: self,
                             previewView: selfieView,
                             useFrontCamera: true,
                             doFlashScreenOnShutter : true)
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        captureSelfie?.start( screenRect: self.view.bounds,
                              userTag:currentTag )
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        guard captureSelfie != nil else {
            return;
        }
        captureSelfie!.stop()
    }
    func onComplete(previewUIImage: UIImage?) {
        selfieViewControllerDelegate?.onSelfieSuccess(tag: currentTag)
        self.dismiss(animated: false, completion:nil)
    }
    
    func onError(sidError: SIDError) {
        let toastUtils = ToastUtils()
        toastUtils.showToast(view: self.view, message: sidError.message )
        selfieViewControllerDelegate?.onSelfieError(error: sidError)
        self.dismiss(animated: false, completion:nil)
    }
    
    func onFaceStateChanged(faceState: Int) {
        // logger.SIPrint(logOutput:  "updatePrompt : faceState = ", faceState )
        switch( faceState ){
            case FaceState.DO_MOVE_CLOSER :
                lblPrompt.text = "Move Closer"
            case FaceState.CAPTURING :
                lblPrompt.text = ""
            case FaceState.DO_SMILE :
                lblPrompt.text = "Smile"
            
            default :
                lblPrompt.text = "Put Your Face Inside Oval";
        } // switch
    }

}
