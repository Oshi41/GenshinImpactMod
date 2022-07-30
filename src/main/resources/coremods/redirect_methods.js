var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
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
                for (var j = 0; j < method.instructions.size(); j++) {

                    var currentInstruction = method.instructions.get(j);

                    if (currentInstruction.getOpcode() === Opcodes.ARETURN) {

                        var list = new InsnList();
                        // loading second parameter from locals (entity)
                        list.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        // calling static method
                        list.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            'com/gim/client/GenshinClientHooks',
                            'getRenderer',
                            '(Lnet/minecraft/client/renderer/entity/EntityRenderer;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/client/renderer/entity/EntityRenderer;',
                            false
                        ));

                        // inserting instructions before ARETURN
                        method.instructions.insertBefore(currentInstruction, list);

                        ASMAPI.log("INFO", 'From Genshin Impact, redirecting EntityRenderDispatcher.getRenderer completed');
                        return method;
                    }
                }

                return method;
            }
        },

        'CombatTracker.<init>': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.LivingEntity',
                'methodName': '<init>',
                'methodDesc': '(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V'
            },
            'transformer': function (method) {
                var desc = '(Lnet/minecraft/world/entity/LivingEntity;)V';
                var type = 'net/minecraft/world/damagesource/CombatTracker';
                var replacingType = 'com/gim/attack/GenshinCombatTracker';

                for (var i = 0; i < method.instructions.size(); i++) {
                    var currentInstruction = method.instructions.get(i);

                    // replacing NEW call
                    if (Opcodes.NEW == currentInstruction.getOpcode() && currentInstruction.desc === type) {
                        method.instructions.set(currentInstruction, new TypeInsnNode(Opcodes.NEW, replacingType));
                    }

                    // replacting ctor call
                    if (Opcodes.INVOKESPECIAL == currentInstruction.getOpcode() && currentInstruction.desc === desc) {
                        var ctorCall = ASMAPI.buildMethodCall(
                            replacingType,
                            currentInstruction.name,
                            currentInstruction.desc,
                            ASMAPI.MethodType.SPECIAL
                        );
                        // replacing ctor call
                        method.instructions.set(currentInstruction, ctorCall);

                        ASMAPI.log("INFO", 'From Genshin Impact, LivingEntity.<init> redirecting LivingEntity combatTracker creation completed');
                    }
                }

                return method;
            }
        },

        'AttributeMap.<init>': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.LivingEntity',
                'methodName': '<init>',
                'methodDesc': '(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V'
            },
            'transformer': function (method) {
                var desc = '(Lnet/minecraft/world/entity/ai/attributes/AttributeSupplier;)V';
                var type = 'net/minecraft/world/entity/ai/attributes/AttributeMap';
                var replacingType = 'com/gim/capability/genshin/GenshinAttributeMap';

                for (var i = 0; i < method.instructions.size(); i++) {
                    var currentInstruction = method.instructions.get(i);

                    // replacing NEW call
                    if (Opcodes.NEW === currentInstruction.getOpcode() && currentInstruction.desc === type) {
                        method.instructions.set(currentInstruction, new TypeInsnNode(Opcodes.NEW, replacingType));
                    }

                    // replacting ctor call
                    if (Opcodes.INVOKESPECIAL === currentInstruction.getOpcode() && currentInstruction.desc === desc) {
                        var ctorCall = ASMAPI.buildMethodCall(
                            replacingType,
                            currentInstruction.name,
                            currentInstruction.desc,
                            ASMAPI.MethodType.SPECIAL
                        );
                        // replacing ctor call
                        method.instructions.set(currentInstruction, ctorCall);

                        ASMAPI.log("INFO", 'From Genshin Impact, LivingEntity.<init> redirecting LivingEntity attribute map creation completed');
                    }
                }

                return method;
            }
        },

        'Effects.HashMap.<init>': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.world.entity.LivingEntity',
                'methodName': '<init>',
                'methodDesc': '(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V'
            },
            'transformer': function (method) {
                for (var i = 0; i < method.instructions.size(); i++) {
                    var currentInstruction = method.instructions.get(i);
                    if (Opcodes.INVOKESTATIC === currentInstruction.getOpcode()
                        && currentInstruction.owner === 'com/google/common/collect/Maps'
                        && currentInstruction.desc === '()Ljava/util/HashMap;') {

                        var list = new InsnList();
                        list.add(new TypeInsnNode(Opcodes.NEW, 'com/gim/capability/genshin/ObservableMap'));
                        list.add(new InsnNode(Opcodes.DUP));
                        list.add(new MethodInsnNode(
                            Opcodes.INVOKESPECIAL,
                            'com/gim/capability/genshin/ObservableMap',
                            '<init>',
                            '()V',
                            false
                        ));

                        method.instructions.insertBefore(currentInstruction, list);
                        method.instructions.remove(currentInstruction);

                        ASMAPI.log("INFO", 'From Genshin Impact, LivingEntity.<init> redirecting LivingEntity effects map creation completed');
                    }
                }

                return method;
            }
        },

        'GameTestRunner.runTestBatches': {
            'target': {
                'type': 'METHOD',
                'class': 'net.minecraft.gametest.framework.GameTestRunner',
                'methodName': 'runTestBatches',
                'methodDesc': '(Ljava/util/Collection;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/gametest/framework/GameTestTicker;I)Ljava/util/Collection;'
            },
            'transformer': function (method) {
                var desc = '(Ljava/util/Collection;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Rotation;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/gametest/framework/GameTestTicker;I)V';
                var type = 'net/minecraft/gametest/framework/GameTestBatchRunner';
                var replacingType = 'com/gim/tests/register/CustomGameTestBatchRunner';

                for (var i = 0; i < method.instructions.size(); i++) {
                    var currentInstruction = method.instructions.get(i);

                    // replacing NEW call
                    if (Opcodes.NEW == currentInstruction.getOpcode() && currentInstruction.desc === type) {
                        method.instructions.set(currentInstruction, new TypeInsnNode(Opcodes.NEW, replacingType));
                    }

                    // replacting ctor call
                    if (Opcodes.INVOKESPECIAL == currentInstruction.getOpcode() && currentInstruction.desc === desc) {
                        var ctorCall = ASMAPI.buildMethodCall(
                            replacingType,
                            currentInstruction.name,
                            currentInstruction.desc,
                            ASMAPI.MethodType.SPECIAL
                        );
                        // replacing ctor call
                        method.instructions.set(currentInstruction, ctorCall);

                        ASMAPI.log("INFO", 'From Genshin Impact, GameTestRunner.runTestBatches redirecting GameTestBatchRunner to CustomGameTestBatchRunner ctor');
                    }
                }

                return method;
            }
        },

        //
        // 'render': {
        //     'target': {
        //         'type': 'METHOD',
        //         'class': 'net.minecraft.client.particle.ParticleEngine',
        //         'methodName': 'render',
        //         'methodDesc': '(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V'
        //     },
        //     'transformer': function (method) {
        //         ASMAPI.log("INFO", 'From Genshin Impact, adding call to ParticleEngine.render');
        //         ASMAPI.log("INFO", ASMAPI.methodNodeToString(method));
        //
        //         var list = new InsnList();
        //         list.add(
        //             ASMAPI.buildMethodCall(
        //                 'com/gim/client/GenshinClientHooks',
        //                 'specialRender',
        //                 '(Lnet/minecraft/client/particle/Particle;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;F)V',
        //                 ASMAPI.MethodType.STATIC
        //             )
        //         );
        //
        //         var methodCall = ASMAPI.findFirstMethodCall(
        //             method,
        //             ASMAPI.MethodType.VIRTUAL,
        //             'net/minecraft/client/particle/Particle',
        //             'render',
        //             '(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V');
        //
        //         if (methodCall) {
        //             ASMAPI.log("INFO", 'Render method founded');
        //
        //             method.instructions.insert(methodCall.getNext().getNext(), list);
        //         }
        //
        //         return method;
        //     }
        // }
    }
}