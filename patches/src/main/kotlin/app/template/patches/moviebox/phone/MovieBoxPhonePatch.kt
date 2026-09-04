package app.template.patches.moviebox.phone

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOX_COMPATIBILITY
import app.template.patches.shared.Constants.MOVIEBOXIN_COMPATIBILITY
import app.template.patches.shared.clearBody

@Suppress("unused")
val movieBoxPhonePatch = bytecodePatch(
    name = "All-In-One",
    description = "Unlocks VIP premium, removes ads and upsells, bypasses region lock " +
        "and force update, unlocks HD and downloads, enables 5 parallel downloads."
) {
    compatibleWith(MOVIEBOX_COMPATIBILITY, MOVIEBOXIN_COMPATIBILITY)

    execute {
        val returnTrue = "const/4 v0, 0x1\nreturn v0"
        val returnFalse = "const/4 v0, 0x0\nreturn v0"
        val returnIntMax = "const/high16 v0, 0x7fff0000\nreturn v0"
        val returnBoxedTrue = "sget-object v0, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;\nreturn-object v0"
        val returnBoxedFalse = "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;\nreturn-object v0"
        val returnInt9999 = """
            const/16 v0, 0x270f
            invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v0
            return-object v0
        """.trimIndent()
        val returnZeroBoxed = """
            const/4 v0, 0x0
            invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v0
            return-object v0
        """.trimIndent()

        // ─── MemberCheckResult (smali_classes7) ──────────
        val memberCheckResult = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberCheckResult;")
            ?: throw PatchException("MemberCheckResult not found")
        for (name in listOf("isPassed", "getVipEnable", "getVipPayEnable")) {
            memberCheckResult.methods.firstOrNull {
                it.name == name && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
            }?.addInstructions(0, returnBoxedTrue)
                ?: throw PatchException("MemberCheckResult.$name() not found")
        }

        // ─── MemberInfo (smali_classes7) ──────────────────
        val memberInfo = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberInfo;")
            ?: throw PatchException("MemberInfo not found")
        memberInfo.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberInfo.isActive()Z not found")
        memberInfo.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        memberInfo.methods.firstOrNull {
            it.name == "getDaysLeft" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnInt9999)
            ?: throw PatchException("MemberInfo.getDaysLeft() not found")
        memberInfo.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")
            ?: throw PatchException("MemberInfo.getExpiryDate() not found")
        memberInfo.methods.firstOrNull {
            it.name == "getNextRenewDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")

        // ─── MemberBriefInfo (smali_classes7) ─────────────
        val memberBriefInfo = mutableClassDefByOrNull("Lcom/transsion/member/bean/MemberBriefInfo;")
            ?: throw PatchException("MemberBriefInfo not found")
        memberBriefInfo.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
        memberBriefInfo.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        memberBriefInfo.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")

        // ─── MemberProvider (smali) ──────────────────────
        val memberProvider = mutableClassDefByOrNull("Lcom/transsion/member/MemberProvider;")
            ?: throw PatchException("MemberProvider not found")

        memberProvider.methods.firstOrNull { it.name == "B" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberProvider.isActive (B()Z) not found")

        memberProvider.methods.firstOrNull { it.name == "h" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberProvider.pay_enable (h()Z) not found")

        memberProvider.methods.firstOrNull { it.name == "g" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberProvider.skip_ad (g()Z) not found")

        memberProvider.methods.firstOrNull { it.name == "z" && it.returnType == "V" && it.parameterTypes == listOf("F") }
            ?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("MemberProvider.showMemberDialog (z(F)V) not found")

        memberProvider.methods.firstOrNull { it.name == "D" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, "const/4 v0, 0x5\nreturn v0")
            ?: throw PatchException("MemberProvider.parallel_download (D()I) not found")

        // ─── PremiumProvider (smali_classes7) ──────────────
        val premiumProvider = mutableClassDefByOrNull("Lcom/transsion/member/premium/PremiumProvider;")
            ?: throw PatchException("PremiumProvider not found")

        premiumProvider.methods.firstOrNull { it.name == "b" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("PremiumProvider.isActive (b()Z) not found")

        premiumProvider.methods.firstOrNull { it.name == "j" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("PremiumProvider.isVip (j()Z) not found")

        premiumProvider.methods.firstOrNull { it.name == "u" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("PremiumProvider.isSVip (u()Z) not found")

        premiumProvider.methods.firstOrNull { it.name == "n" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnInt9999)
            ?: throw PatchException("PremiumProvider.daysLeft (n()Integer) not found")

        premiumProvider.methods.firstOrNull { it.name == "f" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
            ?: throw PatchException("PremiumProvider.free_download_count (f()I) not found")

        premiumProvider.methods.firstOrNull { it.name == "h" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, "const/4 v0, 0x5\nreturn v0")
            ?: throw PatchException("PremiumProvider.per_download_resource_count (h()I) not found")

        premiumProvider.methods.firstOrNull { it.name == "t" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
            ?: throw PatchException("PremiumProvider.max_resolution (t()I) not found")

        premiumProvider.methods.firstOrNull { it.name == "w" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
            ?: throw PatchException("PremiumProvider.preview_seconds (w()I) not found")

        premiumProvider.methods.firstOrNull { it.name == "x" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
            ?: throw PatchException("PremiumProvider.free_hd_preview_count (x()I) not found")

        // ─── NationalInformationManager (smali_classes7) ──
        val nationalInfo = mutableClassDefByOrNull("Lcom/transsion/ad/strategy/NationalInformationManager;")
            ?: throw PatchException("NationalInformationManager not found")
        nationalInfo.methods.firstOrNull {
            it.name == "e" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"90101\"\nreturn-object v0")
            ?: throw PatchException("NationalInformationManager.e() not found")

        // ─── ObserveLoginAction (smali) ────────────────────
        val observeLogin = mutableClassDefByOrNull("Lcom/transsion/member/ObserveLoginAction;")
            ?: throw PatchException("ObserveLoginAction not found")
        observeLogin.methods.firstOrNull {
            it.name == "onLogout" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("ObserveLoginAction.onLogout()V not found")

        // ─── PremiumV2CheckAccessDto (smali_classes7) ─────
        val premiumV2 = mutableClassDefByOrNull("Lcom/transsion/memberapi/PremiumV2CheckAccessDto;")
            ?: throw PatchException("PremiumV2CheckAccessDto not found")
        premiumV2.methods.firstOrNull {
            it.name == "getHasAccess" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("PremiumV2CheckAccessDto.getHasAccess() not found")

        // ─── MemberResolutionBean (smali_classes7) ────────
        val resolutionBean = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("MemberResolutionBean not found")
        resolutionBean.methods.firstOrNull {
            it.name == "isUnlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("MemberResolutionBean.isUnlock() not found")
        resolutionBean.methods.firstOrNull {
            it.name == "getVipResolutionTip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedFalse)
            ?: throw PatchException("MemberResolutionBean.getVipResolutionTip() not found")

        // ─── MemberResolutionDao$DefaultImpls ────────────
        mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionDao\$DefaultImpls;")
            ?.methods?.firstOrNull { it.name == "b" && it.returnType == "Ljava/lang/Object;" && it.parameterTypes.size == 6 }
            ?.apply {
                clearBody()
                addInstructions(0, "sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;\nreturn-object v0")
            }

        // ─── Download paywall ────────────────────────────
        for (className in listOf(
            "Lcom/transsion/baselib/db/download/DownloadBean;",
            "Lcom/transsion/baselib/db/download/VipInfo;",
            "Lcom/transsion/moviedetailapi/DownloadItem;"
        )) {
            mutableClassDefByOrNull(className)
                ?.methods?.firstOrNull {
                    it.name == "getRequireMemberType" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty()
                }?.addInstructions(0, returnZeroBoxed)
        }
        mutableClassDefByOrNull("Lcom/transsion/moviedetailapi/bean/DownloadResolutionItem;")
            ?.methods?.firstOrNull {
                it.name == "getRequireMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
            }?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")

        // ─── ShortTV paywall ─────────────────────────────
        for (className in listOf(
            "Lcom/transsion/shorttv/bean/ShortTVItem;",
            "Lcom/transsion/shorttv/bean/Subject;"
        )) {
            mutableClassDefByOrNull(className)
                ?.methods?.firstOrNull {
                    it.name == "getNeedPaid" && it.returnType == "I" && it.parameterTypes.isEmpty()
                }?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ─── AppLifeStatusInterceptor (smali) ─────────────
        val interceptor = mutableClassDefByOrNull("Lcom/transsion/baselib/net/AppLifeStatusInterceptor;")
            ?: throw PatchException("AppLifeStatusInterceptor not found")
        interceptor.methods.firstOrNull {
            it.name == "i" && it.returnType == "V" && it.parameterTypes == listOf("Ljava/lang/String;", "Ljava/lang/String;")
        }?.apply { clearBody(); addInstructions(0, "return-void") }
        interceptor.methods.firstOrNull {
            it.name == "j" && it.returnType == "V" && it.parameterTypes == listOf("Ljava/lang/String;", "Ljava/lang/String;")
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("AppLifeStatusInterceptor.j(String,String)V not found")
        interceptor.methods.firstOrNull {
            it.name == "k" && it.returnType == "V" && it.parameterTypes == listOf("Ljava/lang/String;")
        }?.apply { clearBody(); addInstructions(0, "return-void") }
        interceptor.methods.firstOrNull {
            it.name == "n" && it.returnType == "Z" && it.parameterTypes == listOf("Lokhttp3/Interceptor\$Chain;")
        }?.addInstructions(0, returnFalse)
            ?: throw PatchException("AppLifeStatusInterceptor.n(Chain)Z not found")

        // ─── NotAvailableActivity ────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/subroom/activity/NotAvailableActivity;")
            ?.methods?.firstOrNull {
                it.name == "initView" && it.returnType == "V" && it.parameterTypes == listOf("Landroid/os/Bundle;")
            }?.addInstructions(0, "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\nreturn-void")

        // ─── Force update bypass ─────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/version/update/RemoteVersionInfo;")
            ?.let { rv ->
                rv.methods.firstOrNull { it.name == "getForceUpdate" && it.returnType == "Z" }
                    ?.addInstructions(0, returnFalse)
                rv.methods.firstOrNull { it.name == "getHasUpdate" && it.returnType == "Z" }
                    ?.addInstructions(0, returnFalse)
            }

        // ─── SceneInterceptManager (smali_classes7) ──────
        mutableClassDefByOrNull("Lcom/transsion/ad/scene/SceneInterceptManager;")
            ?.methods?.firstOrNull {
                it.name == "a" && it.returnType == "Ljava/lang/Object;" &&
                it.parameterTypes == listOf("Ljava/lang/String;", "Lkotlin/coroutines/Continuation;")
            }?.addInstructions(0, """
                new-instance v0, Lkotlin/Pair;
                const/4 v1, 0x1
                invoke-static {v1}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
                move-result-object v1
                const-string v2, "no ads"
                invoke-direct {v0, v1, v2}, Lkotlin/Pair;-><init>(Ljava/lang/Object;Ljava/lang/Object;)V
                return-object v0
            """.trimIndent())

        // ─── SplashActivity ──────────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/subroom/activity/SplashActivity;")
            ?.methods?.firstOrNull { m ->
                m.returnType == "V" && m.implementation?.instructions?.any {
                    it.toString().contains("startSplashAdLoad\$1")
                } == true
            }?.addInstructions(0, """
                const/4 v0, 0x0
                invoke-direct {p0, v0}, Lcom/transsion/subroom/activity/SplashActivity;->e0(Z)V
                return-void
            """.trimIndent())

        // ─── Mintegral ad executor ───────────────────────
        for ((clsName, methodName) in listOf(
            "Lcom/hisavana/mintegral/executer/MintegralVideo;" to "initVideo",
            "Lcom/hisavana/mintegral/executer/MintegralBanner;" to "showBanner",
            "Lcom/hisavana/mintegral/executer/MintegralNative;" to "initNative",
            "Lcom/hisavana/mintegral/executer/MintegralInterstitial;" to "initInterstitial",
            "Lcom/hisavana/mintegral/executer/MintegralSplash;" to "onSplashStartLoad"
        )) {
            mutableClassDefByOrNull(clsName)
                ?.methods?.firstOrNull { it.name == methodName && it.returnType == "V" }
                ?.addInstructions(0, "return-void")
        }
    }
}
