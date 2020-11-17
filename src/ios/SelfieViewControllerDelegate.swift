//
//  SelfieViewControllerDelegate.swift
//  IonicTestApp
//
//  Created by Japhet Ndhlovu on 2020/11/16.
//

import Foundation
import Smile_Identity_SDK

protocol SelfieViewControllerDelegate {
    func onSelfieSuccess(tag: String)
    func onSelfieError(error: SIDError)
    func onSelfieCancelled(tag: String)
}
