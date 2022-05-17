var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');

function initializeCoreMod() {
    return {
        'get_skin_texture_location': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.client.player.AbstractClientPlayer',
                'methodName': ASMAPI.mapMethod('m_108560_'),
                'methodDesc': '()Lnet/minecraft/resources/ResourceLocation;'
            },

            'transformer': function (method) {

                ASMAPI.log("INFO", 'From Genshin Impact, redirecting AbstractClientPlayer.getSkinTextureLocation')

                var newMethod = new MethodNode(
                    /* access = */ method.access,
                    /* name = */ method.name,
                    /* descriptor = */ method.desc,
                    /* signature = */ method.desc,
                    /* exceptions = */ null
                );

                newMethod.visitVarInsn(Opcodes.ALOAD, 0); /*loading this*/
                newMethod.visitMethodInsn(Opcodes.INVOKESTATIC, 'com/gim/client/GenshinClientHooks', 'getSkinTextureLocation',
                    '(Lnet/minecraft/client/player/AbstractClientPlayer;)Lnet/minecraft/resources/ResourceLocation;', false);
                newMethod.visitInsn(Opcodes.ARETURN);

                return newMethod;
            }
        },

        'getModel': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.LivingEntityRenderer'
            },
            'transformer': function (classNode) {
                classNode.methods.forEach(function (methodNode) {
                    if (methodNode.name === 'getModel') {
                        ASMAPI.log("INFO", 'From Genshin Impact, redirecting LivingEntityRenderer.getModel');


                        methodNode.instructions.clear();

                        methodNode.visitMaxs(2, 2);
                        methodNode.visitVarInsn(Opcodes.ALOAD, 0); /*loading this*/
                        methodNode.visitFieldInsn(Opcodes.GETFIELD, classNode.name, "model", "Lnet/minecraft/client/model/EntityModel;"); /*loading this.model*/
                        methodNode.visitVarInsn(Opcodes.ALOAD, 0); /*this.model*/
                        methodNode.visitMethodInsn(Opcodes.INVOKESTATIC, 'com/gim/client/GenshinClientHooks', 'getModel', '(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;)Lnet/minecraft/client/model/EntityModel;', false);
                        methodNode.visitInsn(Opcodes.ARETURN);
                    }
                })

                return classNode;
            }
        }

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