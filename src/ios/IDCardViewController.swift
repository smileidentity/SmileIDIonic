//
//  IDCardViewController.swift
//  IonicTestApp
//
//  Created by Japhet Ndhlovu on 2020/11/16.
//

import UIKit
import AVFoundation
import Smile_Identity_SDK

class IDCardViewController: UIViewController ,CaptureIDCardDelegate {
    
    @IBOutlet weak var lblPrompts: UILabel!
    var iDCardViewControllerDelegate : IDCardViewControllerDelegate?
    var currentTag       : String  = ""
    
    @IBAction func onClickYesButton(_ sender: Any) {
        iDCardViewControllerDelegate?.onIdCardSuccess(tag: currentTag)
        dismiss(animated: false, completion:nil)
    }
   
    @IBAction func cancel(_ sender: Any) {
        dismiss(animated: false, completion: nil)
    }
    @IBAction func onClickNoButton(_ sender: Any) {
        self.view.bringSubviewToFront(previewView)
        self.view.bringSubviewToFront(lblPrompts)

        lblPrompts.text = TAP_SCREEN_TO_CAPTURE
        captureIDCard?.start()
    }
    
    @IBOutlet weak var capturedImageView: UIImageView!
    @IBOutlet weak var capturedImageContainerView: UIView!
    @IBOutlet weak var previewView: CaptureIDCardVideoPreview!
    
    
    @IBOutlet weak var lblPromptsBottom: UILabel!
    
    @IBOutlet weak var maskView         : UIView!
    var captureIDCard                   : CaptureIDCard?
    
    private let FIT_ID_CARD_IN_RECT     : String = "Fit ID card inside rectangle"
    private let TAP_SCREEN_TO_CAPTURE   : String = "Tap inside screen to capture"
    private let NO_FACE_FOUND           : String = "No face found in ID card"
    private let READ_COMPLETE_ID        : String = "Can you read the complete ID?"
    private var firstTime               : Bool = true
    var isReEnroll                      : Bool?
     
    override func viewDidLoad() {
        super.viewDidLoad()
        let value = UIInterfaceOrientation.landscapeRight.rawValue
        UIDevice.current.setValue(value, forKey: "orientation")
     }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        self.view.bringSubviewToFront(previewView)
        self.view.bringSubviewToFront(lblPrompts)
        lblPrompts.text = FIT_ID_CARD_IN_RECT
        DispatchQueue.main.asyncAfter(deadline: .now() + 6.0) {
           self.lblPrompts.text = self.TAP_SCREEN_TO_CAPTURE
        }
        
        /* Unwind segue */
        self.navigationItem.leftBarButtonItem = UIBarButtonItem(title: "Back", style: .done, target: self, action: #selector(self.backButtonPressed(sender:)))
    
//        AppDelegate.AppUtility.lockOrientation(
//            UIInterfaceOrientationMask.landscapeRight, andRotateTo: UIInterfaceOrientation.landscapeRight)
        

    }
    
  
    
 
    
    @objc func backButtonPressed(sender:UIButton) {

        /* The "unwindToSmileID" segue is defined in SmileIDViewController */
        self.performSegue(withIdentifier: "unwindToSmileID", sender: self)
    }
   
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
 
        captureIDCard = CaptureIDCard()
        captureIDCard?.setup(captureIDCardDelegate: self,
                            previewView: previewView,
                            userTag:SmileIDSingleton.DEFAULT_USER_TAG )
        captureIDCard?.start()
       
    }
    
    
    override func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
         captureIDCard?.stop()
        let value = UIInterfaceOrientation.portrait.rawValue
        UIDevice.current.setValue(value, forKey: "orientation")
    }
    
 
    func canRotate() -> Void {}
    
    

    func showPreview() {
        self.view.bringSubviewToFront(capturedImageContainerView)
        lblPromptsBottom.text = READ_COMPLETE_ID
    }

    func onComplete( previewUIImage: UIImage, faceFound : Bool ) {
        capturedImageView.image = previewUIImage
        
        
        // Give the user a chance to accept the card image,
        // even though there is no face image on the card.
        if( !faceFound ){
            if( firstTime ){
                firstTime = false
                lblPrompts.text = NO_FACE_FOUND
            }
            else{
                showPreview()
            }
        }
        else{
            showPreview()
        }
        
    }
        
    func onError( sidError : SIDError ){
        let toastUtils = ToastUtils()
        toastUtils.showToast(view: self.view, message: sidError.message )
        iDCardViewControllerDelegate?.onIdCardError(error: sidError)
        dismiss(animated: false, completion:nil)
    }
    
    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        return .landscapeLeft
    }

    override var shouldAutorotate: Bool {
        return true
    }
    
}

extension UINavigationController {

override open var shouldAutorotate: Bool {
    get {
        if let visibleVC = visibleViewController {
            return visibleVC.shouldAutorotate
        }
        return super.shouldAutorotate
    }
}

override open var preferredInterfaceOrientationForPresentation: UIInterfaceOrientation{
    get {
        if let visibleVC = visibleViewController {
            return visibleVC.preferredInterfaceOrientationForPresentation
        }
        return super.preferredInterfaceOrientationForPresentation
    }
}

override open var supportedInterfaceOrientations: UIInterfaceOrientationMask{
    get {
        if let visibleVC = visibleViewController {
            return visibleVC.supportedInterfaceOrientations
        }
        return super.supportedInterfaceOrientations
    }
}}
