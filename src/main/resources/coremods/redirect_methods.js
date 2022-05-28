var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');

function initializeCoreMod() {
    return {
        'getRender': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.renderer.entity.EntityRenderDispatcher',
                'methodName': ASMAPI.mapMethod('m_114382_'),
                'methodDesc': '(Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;'
            },
            'transformer': function (method) {
                ASMAPI.log("INFO", 'From Genshin Impact, redirecting EntityRenderDispatcher.getRenderer');

                for (var j = 0; j < method.instructions.size(); j++) {

                    var currentInstruction = method.instructions.get(j);

                    if (currentInstruction.getOpcode() === Opcodes.ARETURN) {
                        ASMAPI.log("INFO", 'Founded return statement');

                        var list = new InsnList();
                        // loading parameter (entity)
                        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        list.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'com/gim/client/GenshinClientHooks',
                            'getRenderer',
                            '(Lnet/minecraft/client/renderer/entity/EntityRenderer;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;',
                            false
                        ));
                        list.add(new InsnNode(Opcodes.ARETURN));

                        // inserting instructions before
                        method.instructions.insertBefore(currentInstruction, list);
                        // removing return instruction
                        method.instructions.remove(currentInstruction);
                        return method;
                    }
                }

                return method;
            }
        }
        // 'get_skin_texture_location': {
        //     'target': {
        //         'type': 'METHOD',
        //         'class': 'net.minecraft.client.player.AbstractClientPlayer',
        //         'methodName': ASMAPI.mapMethod('m_108560_'),
        //         'methodDesc': '()Lnet/minecraft/resources/ResourceLocation;'
        //     },
        //
        //     'transformer': function (method) {
        //
        //         ASMAPI.log("INFO", 'From Genshin Impact, redirecting AbstractClientPlayer.getSkinTextureLocation')
        //
        //         var newMethod = new MethodNode(
        //             /* access = */ method.access,
        //             /* name = */ method.name,
        //             /* descriptor = */ method.desc,
        //             /* signature = */ method.desc,
        //             /* exceptions = */ null
        //         );
        //
        //         newMethod.visitVarInsn(Opcodes.ALOAD, 0); /*loading this*/
        //         newMethod.visitMethodInsn(Opcodes.INVOKESTATIC, 'com/gim/client/GenshinClientHooks', 'getSkinTextureLocation',
        //             '(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;', false);
        //         newMethod.visitInsn(Opcodes.ARETURN);
        //
        //         return newMethod;
        //     }
        // },
        //
        // 'getModel': {
        //     'target': {
        //         'type': 'CLASS',
        //         'name': 'net.minecraft.client.renderer.entity.LivingEntityRenderer'
        //     },
        //     'transformer': function (classNode) {
        //         classNode.methods.forEach(function (methodNode) {
        //             if (methodNode.name === 'getModel') {
        //                 ASMAPI.log("INFO", 'From Genshin Impact, redirecting LivingEntityRenderer.getModel');
        //
        //
        //                 methodNode.instructions.clear();
        //
        //                 methodNode.visitMaxs(2, 2);
        //                 methodNode.visitVarInsn(Opcodes.ALOAD, 0); /*loading this*/
        //                 methodNode.visitFieldInsn(Opcodes.GETFIELD, classNode.name, "model", "Lnet/minecraft/client/model/EntityModel;"); /*loading this.model*/
        //                 methodNode.visitVarInsn(Opcodes.ALOAD, 0); /*this.model*/
        //                 methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, 'com/gim/client/GenshinClientHooks', 'getModel', '(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;)Lnet/minecraft/client/model/EntityModel;', false);
        //                 methodNode.visitInsn(Opcodes.ARETURN);
        //             }
        //         })
        //
        //         return classNode;
        //     }
        // }

        // 'getModel': {
        //     'target': {
        //         'type': 'METHOD',
        //         'class': 'net.minecraft.client.renderer.entity.LivingEntityRenderer',
        //         'methodName': ASMAPI.mapMethod('m_7200_'),
        //         'methodDesc': '()Lnet/minecraft/client/model/EntityModel;'
        //     },
        //     'transformer': function (method) {
        //
        //         ASMAPI.log("INFO", 'From Genshin Impact, redirecting LivingEntityRenderer.getModel')
        //
        //         var newMethod = new MethodNode(
        //             /* access = */ method.access,
        //             /* name = */ method.name,
        //             /* descriptor = */ method.desc,
        //             /* signature = */ method.desc,
        //             /* exceptions = */ null
        //         );
        //
        //         method.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0)); /*this*/
        //         method.instructions.add(new VarInsnNode(Opcodes.ILOAD, 0)); /*this as param*/
        //         newMethod.instructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, 'com/gim/client/GenshinClientHooks', 'getModel', '(Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;)Lnet/minecraft/client/model/EntityModel;'));
        //         newMethod.instructions.add(new InsnNode(Opcodes.ARETURN));
        //
        //         return newMethod;
        //     }
        // }
    }
}