package app.template.patches.moviebox.phone

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.PatchException
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.shared.Constants.MOVIEBOX_COMPATIBILITY
import app.template.patches.shared.Constants.MOVIEBOXIN_COMPATIBILITY
import app.template.patches.shared.clearBody
import com.android.tools.smali.dexlib2.Opcode

// ═══════════════════════════════════════════════════════════════════
//  MovieBox Phone v4.0.02.0903.02 (UPDATED)
// ═══════════════════════════════════════════════════════════════════
//
// Updated based on smali analysis (v4.0.02.0903.02):
//   MemberProvider.B()Z  → isActive (was c)
//   MemberProvider.h()Z  → kv_is_pay_enable_member (was f)
//   MemberProvider.g()Z  → kv_is_skip_ad (unchanged)
//   MemberProvider.z(F)V → showMemberDialog (was x)
//   MemberProvider.D()I  → parallel_download_task_num (unchanged)
//   PremiumProvider.b()Z → isActive (was c)
//   PremiumProvider.j()Z → isVip (was k)
//   PremiumProvider.u()Z → isSVip (unchanged)
//   PremiumProvider.n()Ljava/lang/Integer; → daysLeft (was o)
//   PremiumProvider.f()I → free_download_count (unchanged)
//   PremiumProvider.h()I → per_download_resource_count (was i)
//   PremiumProvider.t()I → max_resolution (unchanged)
//   PremiumProvider.w()I → preview_seconds (unchanged)
//   PremiumProvider.x()I → free_hd_preview_count (unchanged)
//   NationalInformationManager.e()Ljava/lang/String; → unchanged

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

        // ─── MemberCheckResult — server membership response ───────────
        var cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberCheckResult;")
            ?: throw PatchException("MemberCheckResult not found")
        for (name in listOf("isPassed", "getVipEnable", "getVipPayEnable")) {
            cls.methods.firstOrNull {
                it.name == name && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
            }?.addInstructions(0, returnBoxedTrue)
                ?: throw PatchException("MemberCheckResult.$name() not found")
        }

        // ─── MemberInfo — full member detail bean ────────────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/MemberInfo;")
            ?: throw PatchException("MemberInfo not found")
        cls.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberInfo.isActive()Z not found")
        cls.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        cls.methods.firstOrNull {
            it.name == "getDaysLeft" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnInt9999)
            ?: throw PatchException("MemberInfo.getDaysLeft() not found")
        cls.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")
            ?: throw PatchException("MemberInfo.getExpiryDate() not found")
        cls.methods.firstOrNull {
            it.name == "getNextRenewDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")

        // ─── MemberBriefInfo — lightweight member summary bean ────────
        cls = mutableClassDefByOrNull("Lcom/transsion/member/bean/MemberBriefInfo;")
            ?: throw PatchException("MemberBriefInfo not found")
        cls.methods.firstOrNull {
            it.name == "isActive" && it.returnType == "Z" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnTrue)
        cls.methods.firstOrNull {
            it.name == "getMemberType" && it.returnType == "I" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const/4 v0, 0x2\nreturn v0")
        cls.methods.firstOrNull {
            it.name == "getExpiryDate" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"2099-12-31\"\nreturn-object v0")

        // ─── MemberProvider — UPDATED FOR v4.0.02.0903.02 ────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/member/MemberProvider;")
            ?: throw PatchException("MemberProvider not found")

        // isActive - B()Z (was c()Z)
        cls.methods.firstOrNull { it.name == "B" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberProvider.isActive (B()Z) not found")

        // kv_is_pay_enable_member - h()Z (was f()Z)
        cls.methods.firstOrNull { it.name == "h" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberProvider.pay_enable (h()Z) not found")

        // kv_is_skip_ad - g()Z (unchanged)
        cls.methods.firstOrNull { it.name == "g" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("MemberProvider.skip_ad (g()Z) not found")

        // showMemberDialog - z(F)V (was x(F)V)
        cls.methods.firstOrNull { it.name == "z" && it.returnType == "V" && it.parameterTypes == listOf("F") }
            ?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("MemberProvider.showMemberDialog (z(F)V) not found")

        // parallel_download_task_num - D()I (unchanged)
        cls.methods.firstOrNull { it.name == "D" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, "const/4 v0, 0x5\nreturn v0")
            ?: throw PatchException("MemberProvider.parallel_download (D()I) not found")

        // ─── PremiumProvider — UPDATED FOR v4.0.02.0903.02 ────────────
        cls = mutableClassDefByOrNull("Lcom/transsion/member/premium/PremiumProvider;")
            ?: throw PatchException("PremiumProvider not found")

        // isActive - b()Z (was c()Z)
        cls.methods.firstOrNull { it.name == "b" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("PremiumProvider.isActive (b()Z) not found")

        // isVip - j()Z (was k()Z)
        cls.methods.firstOrNull { it.name == "j" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("PremiumProvider.isVip (j()Z) not found")

        // isSVip - u()Z (unchanged)
        cls.methods.firstOrNull { it.name == "u" && it.returnType == "Z" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnTrue)
            ?: throw PatchException("PremiumProvider.isSVip (u()Z) not found")

        // daysLeft - n()Ljava/lang/Integer; (was o())
        cls.methods.firstOrNull { it.name == "n" && it.returnType == "Ljava/lang/Integer;" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnInt9999)
            ?: throw PatchException("PremiumProvider.daysLeft (n()Integer) not found")

        // free_download_count - f()I (unchanged)
        cls.methods.firstOrNull { it.name == "f" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
            ?: throw PatchException("PremiumProvider.free_download_count (f()I) not found")

        // per_download_resource_count - h()I (was i()I)
        cls.methods.firstOrNull { it.name == "h" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, "const/4 v0, 0x5\nreturn v0")
            ?: throw PatchException("PremiumProvider.per_download_resource_count (h()I) not found")

        // max_resolution - t()I (unchanged)
        cls.methods.firstOrNull { it.name == "t" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
            ?: throw PatchException("PremiumProvider.max_resolution (t()I) not found")

        // preview_seconds - w()I (unchanged)
        cls.methods.firstOrNull { it.name == "w" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
            ?: throw PatchException("PremiumProvider.preview_seconds (w()I) not found")

        // free_hd_preview_count - x()I (unchanged)
        cls.methods.firstOrNull { it.name == "x" && it.returnType == "I" && it.parameterTypes.isEmpty() }
            ?.addInstructions(0, returnIntMax)
            ?: throw PatchException("PremiumProvider.free_hd_preview_count (x()I) not found")

        // ─── NationalInformationManager — country code spoof ─────────
        cls = mutableClassDefByOrNull("Lcom/transsion/ad/strategy/NationalInformationManager;")
            ?: throw PatchException("NationalInformationManager not found")
        // e()Ljava/lang/String; (unchanged)
        cls.methods.firstOrNull {
            it.name == "e" && it.returnType == "Ljava/lang/String;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, "const-string v0, \"90101\"\nreturn-object v0")
            ?: throw PatchException("NationalInformationManager.e() not found")

        // ─── ObserveLoginAction — prevent logout resetting skip-ad ───
        cls = mutableClassDefByOrNull("Lcom/transsion/member/ObserveLoginAction;")
            ?: throw PatchException("ObserveLoginAction not found")
        cls.methods.firstOrNull {
            it.name == "onLogout" && it.returnType == "V" && it.parameterTypes.isEmpty()
        }?.apply { clearBody(); addInstructions(0, "return-void") }
            ?: throw PatchException("ObserveLoginAction.onLogout()V not found")

        // ─── PremiumV2CheckAccessDto — download access server response
        cls = mutableClassDefByOrNull("Lcom/transsion/memberapi/PremiumV2CheckAccessDto;")
            ?: throw PatchException("PremiumV2CheckAccessDto not found")
        cls.methods.firstOrNull {
            it.name == "getHasAccess" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("PremiumV2CheckAccessDto.getHasAccess() not found")

        // ─── MemberResolutionBean — per-episode HD resolution lock ───
        cls = mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionBean;")
            ?: throw PatchException("MemberResolutionBean not found")
        cls.methods.firstOrNull {
            it.name == "isUnlock" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedTrue)
            ?: throw PatchException("MemberResolutionBean.isUnlock() not found")
        cls.methods.firstOrNull {
            it.name == "getVipResolutionTip" && it.returnType == "Ljava/lang/Boolean;" && it.parameterTypes.isEmpty()
        }?.addInstructions(0, returnBoxedFalse)
            ?: throw PatchException("MemberResolutionBean.getVipResolutionTip() not found")

        // ─── MemberResolutionDao$DefaultImpls — prevent DB vipTip write
        mutableClassDefByOrNull("Lcom/transsion/baselib/db/member/MemberResolutionDao\$DefaultImpls;")
            ?.methods?.firstOrNull { it.name == "b" && it.returnType == "Ljava/lang/Object;" && it.parameterTypes.size == 6 }
            ?.apply {
                clearBody()
                addInstructions(0, "sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;\nreturn-object v0")
            }

        // ─── Download paywall — all getRequireMemberType → 0 ─────────
        for (className in listOf(
            "Lcom/transsion/baselib/db/download/DownloadBean;",
            "Lcom/transsion/baselib/db/download/VipInfo;",
            "Lcom/transsion/moviedetailapi/DownloadItem;",
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

        // ─── ShortTV live stream paywall — getNeedPaid()I → 0 ────────
        for (className in listOf(
            "Lcom/transsion/shorttv/bean/ShortTVItem;",
            "Lcom/transsion/shorttv/bean/Subject;",
        )) {
            mutableClassDefByOrNull(className)
                ?.methods?.firstOrNull {
                    it.name == "getNeedPaid" && it.returnType == "I" && it.parameterTypes.isEmpty()
                }?.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
        }

        // ─── AppLifeStatusInterceptor — region bypass ─────────────────
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

        // ─── NotAvailableActivity — region-lock wall ──────────────────
        mutableClassDefByOrNull("Lcom/transsion/subroom/activity/NotAvailableActivity;")
            ?.methods?.firstOrNull {
                it.name == "initView" && it.returnType == "V" && it.parameterTypes == listOf("Landroid/os/Bundle;")
            }?.addInstructions(0, "invoke-virtual {p0}, Landroid/app/Activity;->finish()V\nreturn-void")

        // ─── Force update bypass ──────────────────────────────────────
        mutableClassDefByOrNull("Lcom/transsion/version/update/RemoteVersionInfo;")
            ?.let { rv ->
                rv.methods.firstOrNull { it.name == "getForceUpdate" && it.returnType == "Z" }
                    ?.addInstructions(0, returnFalse)
                rv.methods.firstOrNull { it.name == "getHasUpdate" && it.returnType == "Z" }
                    ?.addInstructions(0, returnFalse)
            }

        // ─── Scene ad removal — SceneInterceptManager ─────────────────
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

        // ─── Splash ad redirect ────────────────────────────────────────
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

        // ─── Mintegral ad executor kill points ────────────────────────
        for ((cls2, method) in listOf(
            "Lcom/hisavana/mintegral/executer/MintegralVideo;" to "initVideo",
            "Lcom/hisavana/mintegral/executer/MintegralBanner;" to "showBanner",
            "Lcom/hisavana/mintegral/executer/MintegralNative;" to "initNative",
            "Lcom/hisavana/mintegral/executer/MintegralInterstitial;" to "initInterstitial",
            "Lcom/hisavana/mintegral/executer/MintegralSplash;" to "onSplashStartLoad",
        )) {
            mutableClassDefByOrNull(cls2)
                ?.methods?.firstOrNull { it.name == method && it.returnType == "V" }
                ?.addInstructions(0, "return-void")
        }
    }
}
