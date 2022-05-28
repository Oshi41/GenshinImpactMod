var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

function initializeCoreMod() {
    return {
        'getModel_LivingEntityRenderer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.LivingEntityRenderer'
            },
            'transformer': function (classNode) {
                ASMAPI.log("INFO", 'From Genshin Impact, redirecting LivingEntityRenderer.model to LivingEntityRenderer.getModel()')
                return redirectFieldToMethodUnsafe(classNode, ASMAPI.mapField('f_115290_'), ASMAPI.mapMethod('m_7200_')) // model to getModel
            }
        },
        'getModel_PlayerRenderer': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.player.PlayerRenderer'
            },
            'transformer': function (classNode) {
                ASMAPI.log("INFO", 'From Genshin Impact, redirecting PlayerRenderer.model to PlayerRenderer.getModel()')
                var fDesc = 'Lnet/minecraft/client/model/EntityModel;';
                return redirectFieldToMethodUnsafe(classNode, ASMAPI.mapField('f_115290_'), ASMAPI.mapMethod('m_7200_'),
                    fDesc, '()' + fDesc, 'net/minecraft/client/renderer/entity/LivingEntityRenderer') // model to getModel
            }
        }
    };
}

/**
 * Redirects variable call to method call
 * @param classnode - current class node
 * @param fName - name of field
 * @param mName - name of method
 * @param fDesc - optional field description. Can be empty if field declared in this class
 * @param mDesc - optional method description. Can be empty if declared in this class
 * @param mClass - optional field and method owner. . Can be empty if both are declared in this class
 * @returns {*} - tranformered class node
 */
function redirectFieldToMethodUnsafe(classnode, fName, mName, fDesc, mDesc, mClass) {
    if (classnode) {

        if (!fDesc) {
            var field = findField(classnode, fName);
            if (field) {
                fDesc = field.desc;
            }
        }
        if (!mDesc) {
            var method = findMethod(classnode, mName);
            if (method) {
                mDesc = method.desc;
            }
        }
        if (!mClass) {
            mClass = classnode.name;
        }

        var replacingCount = 0;

        if (!fDesc || !mDesc || !mClass) {
            ASMAPI.log("WARN", 'Cannot detect current field or method for provided class');
            return classnode;
        }

        for (i = 0; i < classnode.methods.length; i++) {
            var currentMethod = classnode.methods[i];

            // same method
            if (currentMethod.name === mName && currentMethod.desc === mDesc) {
                continue;
            }

            for (var j = 0; j < currentMethod.instructions.size(); j++) {
                var currentInstruction = currentMethod.instructions.get(j);
                if (currentInstruction.getOpcode() === Opcodes.GETFIELD && currentInstruction.name === fName && currentInstruction.desc === fDesc) {
                    currentMethod.instructions.set(currentInstruction, ASMAPI.buildMethodCall(mClass, mName, mDesc, ASMAPI.MethodType.VIRTUAL));
                    replacingCount = replacingCount + 1;
                }
            }
        }

        if (replacingCount) {
            ASMAPI.log("INFO", "Was made  " + replacingCount + " field to method redirections (" + fName + " --> " + mName + ") in class " + classnode.name);
        }
    }

    return classnode;
}

function findField(classnode, fName) {
    // iterating through all class fields
    for (var i = 0; i < classnode.fields.length; i++) {
        var currentField = classnode.fields[i];

        // founded the same name
        if (currentField.name === fName) {
            return currentField;
        }
    }

    return undefined;
}

function findMethod(classnode, mName) {
    for (var i = 0; i < classnode.methods.length; i++) {
        var method = classnode.methods[i];
        // founded same name and needed signature
        if (method.name === mName && method.desc.startsWith("()")) {
            return method;
        }
    }

    return undefined;
}
