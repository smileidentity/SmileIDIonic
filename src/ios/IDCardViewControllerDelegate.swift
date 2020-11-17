//
//  IDCardViewControllerDelegate.swift
//  IonicTestApp
//
//  Created by Japhet Ndhlovu on 2020/11/16.
//

import Foundation
import Smile_Identity_SDK

protocol IDCardViewControllerDelegate {
    func onIdCardSuccess(tag: String)
    func onIdCardError(error: SIDError)
    func onIdCardCancelled(tag: String)
}
