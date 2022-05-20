var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');

function initializeCoreMod() {
    return {
        'getModel': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.client.renderer.entity.LivingEntityRenderer'
            },
            'transformer': function (classNode) {
                ASMAPI.log("INFO", 'From Genshin Impact, redirecting LivingEntityRenderer.model to LivingEntityRenderer.getModel()')
                return redirectFieldToMethod(classNode, ASMAPI.mapField('f_115290_'), ASMAPI.mapMethod('m_7200_')) // model to getModel
            }
        }
    };
}

/**
 * Redirect first field by name to first suitable method
 * @param classnode - current class node
 * @param fieldNameMapped - already mapped field name
 * @param methodNameMapped - already mapped method name
 * @returns {*}
 */
function redirectFieldToMethod(classnode, fieldNameMapped, methodNameMapped) {
    if (classnode) {
        var field = null;
        var replacingCount = 0;

        // iterating through all class fields
        for (var i = 0; i < classnode.fields.length; i++) {
            var currentField = classnode.fields[i];

            // founded the same name
            if (currentField.name === fieldNameMapped) {
                field = currentField;
                break;
            }
        }

        // if field is present
        if (field) {
            var method = null;

            // iterating through all methods
            for (var j = 0; j < classnode.methods.length; j++) {
                var method1 = classnode.methods[j];
                // founded same name and needed signature
                if (method1.name === methodNameMapped && method1.desc === "()" + field.desc) {
                    method = method1;
                    break;
                }
            }

            // if method is presented
            if (method) {
                // iterating through all class methods
                for (j = 0; j < classnode.methods.length; j++) {
                    var currentMethod = classnode.methods[j];

                    // same method
                    if (currentMethod.name === methodNameMapped && currentMethod.desc === "()" + field.desc) {
                        continue;
                    }

                    for (var k = 0; k < currentMethod.instructions.size(); k++) {
                        var currentInstruction = currentMethod.instructions.get(k);
                        // getting this field
                        if (currentInstruction.getOpcode() === Opcodes.GETFIELD && currentInstruction.name === fieldNameMapped) {
                            currentMethod.instructions.set(currentInstruction, ASMAPI.buildMethodCall(classnode.name, method.name, method.desc, ASMAPI.MethodType.VIRTUAL));
                            replacingCount = replacingCount + 1;
                        }
                    }
                }
            }
        }

        if (replacingCount) {
            ASMAPI.log("INFO", "Was made  " + replacingCount + " field to method redirections (" + fieldNameMapped + " --> " + methodNameMapped + ") in class " + classnode.name);
        }

        return classnode;
    }
}